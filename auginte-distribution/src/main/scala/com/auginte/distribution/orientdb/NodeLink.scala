package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.{Edge, Direction}
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}
import scala.collection.JavaConversions._


/**
 * Manages connection to
 */
trait NodeLink[A] extends NodeWrapper with Persistable[A] { self: A =>

  protected def nodeEdge: (Direction, String)

  //
  // Getting node
  //

  def node(implicit cache: Node.Cached = Node.defaultCache): Node = persisted match {
    case Some(persisted) => edge(nodeEdgeName) match {
      case Some(linked) => cache(linked.getIdentity) match {
        case Some(cached) => cached
        case None => wrapVertex(linked)(persisted, cache)
      }
      case None => fallbackToRootNode(persisted)
    }
    case None => Unexpected.state(s"Using node without OrientDFB persisted: $this")
  }

  private def nodeEdgeName = nodeEdge match {
    case (Direction.OUT, name) => s"out_$name"
    case (Direction.IN, name) => s"in_$name"
    case _ => Unexpected.state(s"NodeLink with unsuported type: $nodeEdge")
  }

  private def wrapVertex(linked: ODocument)(persisted: OrientVertex, cache: Node.Cached): Node = {
    val newNode = Node(persisted.getGraph.getVertex(linked))
    cache += newNode.persisted.get.getIdentity -> newNode
    newNode
  }

  protected def fallbackToRootNode(persisted: OrientVertex) = Position.rootNode(persisted.getGraph)

  private def edge(field: String): Option[ODocument] = CommonSql.edge(persistedDocument.get, field)


  //
  // Setting node
  //

  override def node_=(link: Node): Unit = {
    persistedEdge(link) match {
      case Some((from, to, edges)) => inTransaction(from.getGraph) {
        removeOldEdges(edges)
        create(from, to)
      }
      case None => Unexpected.state(s"Updating link, while not persisted: $persisted -> ${link.persisted}")
    }
  }

  private def persistedEdge(link: Node): Option[(OrientVertex, OrientVertex, Iterable[Edge])] =
    if (persisted.isDefined && link.persisted.isDefined) {
      val from: OrientVertex = persisted.get
      val to: OrientVertex = link.persisted.get
      val edges = from.getEdges(nodeEdge._1, nodeEdge._2).toIterable
      Some(from, to, edges)
    } else None


  private def removeOldEdges(edges: Iterable[Edge]): Unit = edges.foreach(_.remove())

  private def create(from: OrientVertex, to: OrientVertex): Unit = reloadAnd(from, to) {
    from.addEdge(nodeEdge._2, to)
  }
}
