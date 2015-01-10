package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.distribution.orientdb.{Representation => R}
import com.auginte.zooming
import com.auginte.zooming.NodeToNode
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}
import java.{lang => jl}
import scala.collection.JavaConversions._

/**
 * Representing relative position in infinity zooming,
 *
 * Persisting data to OrientDB.
 * Using cache object for links via OrientDb edges.
 */
class Node(val _x: Byte = 0, val _y: Byte = 0, protected val cache: Cache[Node] = Node.defaultCache)
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

  private def edge(field: String): Option[ODocument] = CommonSql.edge(persistedDocument.get, field)

  private def edges(field: String): Iterable[ODocument] = CommonSql.edges(persistedDocument.get, field)

  override def getChild(x: Byte, y: Byte): Option[zooming.Node] =
    if (!isPersisted) super.getChild(x, y)
    else cache(edges("in_Parent").find(d => d.field[Byte]("x") == x && d.field[Byte]("y") == y))


  //
  // Setters
  //

  def sameNode(node: zooming.Node): zooming.Node = node

  override protected[auginte] def createParent()(implicit newNode: NodeToNode = sameNode): zooming.Node =
    persisted match {
      case Some(persisted) =>
        val parentNode = new Node(0, 0, cache)
        val parentVertex = createVertex(Map("x" -> 0.boxed, "y" -> 0.boxed))
        parentNode.persisted = parentVertex
        reloadAnd(persisted, parentVertex){
          persisted.addEdge("Parent", parentVertex)
        }
        newNode(parentNode)
      case None => Unexpected.state(s"Creating parent node from not persisted: $this")
    }

  override protected[auginte] def addChild(x: Byte, y: Byte)(implicit newNode: NodeToNode = sameNode): zooming.Node =
    persisted match {
      case Some(persisted) =>
        val childNode = new Node(x, y, cache)
        val childVertex = createVertex(Map("x" -> x.boxed, "y" -> y.boxed))
        childNode.persisted = childVertex
        cache += childVertex.getRecord -> childNode
        reloadAnd(childVertex, persisted){
          childVertex.addEdge("Parent", persisted)
        }
        newNode(childNode)
      case None => Unexpected.state(s"Creating child node from not persisted: $this")
    }


  //
  // Representation
  //

  override def isChildOf(distantParent: zooming.Node): Boolean = {
    //FIXME: useCache
    true
  }

  def representations(creator: Creator)(implicit cache: Representation.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    persisted match {
      case None => EmptyRepresentationStorageIterable
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

  override def toString() = if (isPersisted) s"{ONode: ${persisted.get}}" else "{Node: x=" + x + ", y=" + y + "}"
}

object Node extends DefaultCache[Node] {
  type Rows = jl.Iterable[ODocument]

  def apply(x: Byte, y: Byte, storage: OrientBaseGraph): Node = new Node(x, y, new Cache())

  def apply(vertex: OrientVertex): Node = new Node() {
    persisted = vertex
  }

  def unapply(data: Node) = Some(data.x, data.y)

  def load(storage: OrientBaseGraph, rows: Rows, field: String = "rid")(implicit cache: Cached = defaultCache): Iterable[Node] =
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
