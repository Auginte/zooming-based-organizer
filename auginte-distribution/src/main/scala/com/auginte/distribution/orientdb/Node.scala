package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.distribution.orientdb.{Representation => R}
import com.auginte.zooming
import com.auginte.zooming.NodeToNode
import com.orientechnologies.orient.core.id.{ORID, ORecordId}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}

import scala.collection.JavaConversions._
import scala.language.implicitConversions

/**
 * Representing relative position in infinity zooming,
 *
 * Persisting data to OrientDB.
 * Using cache object for links via OrientDb edges.
 */
class Node(val _x: Byte = 0, val _y: Byte = 0, protected val cache: Cache[Node] = Node.defaultCache)
  extends zooming.Node(_x, _y) with Persistable[Node] {

  private[auginte] val checkParentsConsistency = false // Useful for testing, but disabled in production

  //
  // Structure
  //

  import PersistableImplicits._

  override protected[orientdb] def tableName: String = "Node"

  override protected[orientdb] def fields = Map[String, (this.type) => Object](
    "x" -> (_.x.boxed),
    "y" -> (_.y.boxed)
  )


  //
  // Zooming
  //

  private def edge(field: String): Option[ORID] = CommonSql.edge(persistedDocument.get, field)

  private def edges(field: String): Iterable[ORID] = CommonSql.edges(persistedDocument.get, field)

  override protected[auginte] def createParent()(implicit newNode: NodeToNode = zooming.sameNode): zooming.Node = {
    val parentNode = super.createParent()(Node.newPersistableNode(cache)).asInstanceOf[Node]
    persisted match {
      case Some(persistedVertex) =>
        val parentVertex = graph.addVertex("class:Node", "x", Byte.box(parentNode.x), "y", Byte.box(parentNode.y))
        parentVertex.attach(graph)
        persistedVertex.addEdge("Parent", parentVertex)
        parentNode.persisted = parentVertex
        cache += parentVertex.getIdentity -> parentNode
        parentVertex.save()
      case None => throw new AssertionError("Trying store parent on not persisted node")
    }
    parentNode
  }

  override protected[auginte] def addChild(x: Byte, y: Byte)(implicit newNode: NodeToNode = zooming.sameNode): zooming.Node =
  {
    val child = super.addChild(x, y)(Node.newPersistableNode(cache)).asInstanceOf[Node]
    persisted match {
      case Some(persistedVertex) =>
        val childVertex = graph.addVertex("class:Node", "x", Byte.box(child.x), "y", Byte.box(child.y))
        childVertex.attach(graph)
        childVertex.addEdge("Parent", persistedVertex)
        child.persisted = childVertex
        childVertex.save()
        cache += childVertex.getIdentity -> child
      case None => throw new AssertionError("Trying store child on not persisted node")
    }
    child
  }

  private def graph: OrientBaseGraph = ThreadedDb.getDefault match {
    case Some(g) => g
    case None => persisted match {
      case Some(p) if p.getGraph != null => p.getGraph
      case other => throw new AssertionError("Graph not passed or retrievable from persited")
    }
  }

  //
  // Representation
  //

  def representations(creator: Creator)(implicit cache: R.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    persisted match {
      case None => EmptyRepresentationStorageIterable
      case Some(persisted) => edges("in_Inside") map { edge =>
        cache(edge.getIdentity) match {
          case Some(cached) => cached
          case None => Representation.load(ThreadedDb.getGraph(persisted).getVertex(edge), creator)
        }
      }
    }

  def updateInMemoryRelations(implicit cache: Node.Cached = Node.defaultCache) = persisted match {
    case Some(vertex) =>
      val edges = vertex.getEdges(Direction.IN, "Parent")
      for (edge <- edges) {
        val childVertex = edge.getVertex(Direction.OUT)
        val id = childVertex.getId.asInstanceOf[ORecordId]
        cache(id.getIdentity) match {
          case Some(node) => this.addExistingChild(node)
          case None => throw new AssertionError("Node by ORID not found in cache")
        }
      }
    case None => throw new AssertionError("When updating relations, expected node to be persisted")
  }


  //
  // Miscellaneous
  //


  override def toString() = if (isPersisted) s"{ONode: ${persisted.get}}" else "{Node: x=" + x + ", y=" + y + "}"
}

object Node extends DefaultCache[Node] {
  type Rows = jl.Iterable[ODocument]

  private def newPersistableNode(cache: Cache[Node]):NodeToNode = (n) => new Node(n.x, n.y, cache)

  def apply(x: Byte, y: Byte, storage: OrientBaseGraph): Node = new Node(x, y, new Cache())

  def apply(n: Node, _checkParentsConsistency: Boolean): Node = new Node(n.x, n.y, n.cache) {
    override val checkParentsConsistency = _checkParentsConsistency

    n.persisted match {
      case Some(p) => persisted = p
      case _ => Unit
    }
  }

  def apply(vertex: OrientVertex)(implicit cache: Cached = defaultCache): Node = new Node() {
    //FIXME: depnedency inection for cache
    cache += vertex.getIdentity -> this
    persisted = vertex
  }

  def unapply(data: Node) = Some(data.x, data.y)

  def load(storage: OrientBaseGraph, rows: Rows, field: String = "rid")(implicit cache: Cached = defaultCache): Iterable[Node] =
    iterableAsScalaIterable(rows) flatMap { row =>
      try {
        val record = row.field[ODocument](field)
        val loadedNode = new Node(record.field[Byte]("x"), record.field[Byte]("y"))
        val loadedVertex = storage.getVertex(record)
        loadedNode.persisted = loadedVertex
        cache += record.getIdentity -> loadedNode
        Some(loadedNode)
      } catch {
        case e: Exception => None
      }
    }
}
