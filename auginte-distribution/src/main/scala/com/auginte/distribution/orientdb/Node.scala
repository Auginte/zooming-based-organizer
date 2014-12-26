package com.auginte.distribution.orientdb

import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.distribution.orientdb.{Representation => R}
import com.auginte.zooming
import com.auginte.zooming.NodeToNode
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph
import java.{lang => jl}
import scala.collection.JavaConversions._

/**
 * Representing relative position in infinity zooming,
 *
 * Persisting data to OrientDB.
 * Using cache object for links via OrientDb edges.
 */
class Node(val _x: Byte = 0, val _y: Byte = 0, protected val cache: Cache[Node] = new Cache[Node])
  extends zooming.Node(_x, _y) with Persistable[Node] {


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
  // Getters
  //

  override def x: Byte = get[Byte]("x", _x)

  override def y: Byte = get[Byte]("y", _y)

  override def parent: Option[zooming.Node] = if (isPersisted) cache(edge("out_Parent")) else super.parent

  override def children: List[zooming.Node] = if (isPersisted) cache(edges("in_Parent")).toList else super.children

  private def edge(field: String): Option[ODocument] = {
    val links = persistedDocument.get.field[ORidBag](field)
    if (links == null || links.isEmpty) None
    else Some(links.iterator().next().getRecord[ODocument])
  }

  private def edges(field: String): Iterable[ODocument] = {
    val links = persistedDocument.get.field[ORidBag](field)
    if (links == null || links.isEmpty) EmptyDocumentIterable
    else proxyIterable[OIdentifiable, ODocument](links, _.getRecord[ODocument])
  }

  override def getChild(x: Byte, y: Byte): Option[zooming.Node] =
    if (!isPersisted) super.getChild(x, y)
    else cache(edges("in_Parent").find(d => d.field[Byte]("x") == x && d.field[Byte]("y") == y))


  //
  // Setters
  //

  def sameNode(node: zooming.Node): zooming.Node = node

  override protected[auginte] def createParent()(implicit newNode: NodeToNode = sameNode): zooming.Node =
    if (!isPersisted) super.createParent()(newNode)
    else {
      val parentNode = new Node(0, 0, cache)
      val parentVertex = createVertex(Map("x" -> 0.boxed, "y" -> 0.boxed))
      parentNode.persisted = parentVertex
      cache += parentVertex.getRecord -> parentNode
      persisted.get.addEdge("Parent", parentVertex)
      newNode(parentNode)
    }

  override protected[auginte] def addChild(x: Byte, y: Byte)(implicit newNode: NodeToNode = sameNode): zooming.Node =
    if (!isPersisted) super.addChild(x, y)(newNode)
    else {
      val childNode = new Node(x, y, cache)
      val childVertex = createVertex(Map("x" -> x.boxed, "y" -> y.boxed))
      childNode.persisted = childVertex
      cache += childVertex.getRecord -> childNode
      childVertex.addEdge("Parent", persisted.get)
      newNode(childNode)
    }


  //
  // Representation
  //

  def representations(creator: Creator)(implicit cache: Representation.Cached = R.defaultCache): Iterable[Representation] =
    persisted match {
      case None => EmptyRepresentationIterable
      case Some(persisted) => edges("in_Inside") map { edge =>
        cache(edge) match {
          case Some(cached) => cached
          case None => Representation.load(persisted.getGraph.getVertex(edge), creator)
        }
      }
    }


  //
  // Miscellaneous
  //

  override def toString() = "{Node: x=" + x + ", y=" + y + "}"
}

object Node extends DefaultCache[Node] {
  type Rows = jl.Iterable[ODocument]

  type Cached = Cache[Node]

  def apply(x: Byte, y: Byte, storage: OrientBaseGraph): Node = new Node(x, y, new Cache())

  def unapply(data: Node) = Some(data.x, data.y)

  def load(storage: OrientBaseGraph, rows: Rows, field: String = "rid")(cache: Cached = defaultCache): Iterable[Node] =
    iterableAsScalaIterable(rows) flatMap { row => try {
      val record = row.field[ODocument](field)
      cache(record) match {
        case node: Some[Node] => node
        case None => try {
          val loadedNode = new Node(record.field[Byte]("x"), record.field[Byte]("y"), cache)
          val loadedVertex = storage.getVertex(record)
          loadedNode.persisted = loadedVertex
          cache += loadedVertex.getRecord -> loadedNode
          val rez = Some(cache(record))
          Some(loadedNode)
        } catch {
          case e: Exception => None
        }
      }
    } catch {
      case e: Exception => None
    }
  }
}
