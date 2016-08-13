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
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}
import java.{lang => jl}

import scala.collection.JavaConversions._
import com.auginte.distribution.orientdb.CommonSql._
import com.orientechnologies.orient.core.id.ORID

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

  private var loaded = false
  private var cachedParent: Option[zooming.Node] = None

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


  override def parent: Option[zooming.Node] = if (loaded) {
    cachedParent
  } else {
    if (isPersisted) {
      val rez = cache(edge("out_Parent"))
      cachedParent = rez
      if (rez.isDefined) {
        loaded = true
      }
      rez
    } else super.parent
  }

  override def children: List[zooming.Node] = if (isPersisted) cache(edges("in_Parent")).toList else super.children

  private def edge(field: String): Option[ORID] = CommonSql.edge(persistedDocument.get, field)

  private def edges(field: String): Iterable[ORID] = CommonSql.edges(persistedDocument.get, field)

  override def getChild(x: Byte, y: Byte): Option[zooming.Node] =
    if (!isPersisted) super.getChild(x, y)
    else cache(edges("in_Parent")) match {
      case children if children.nonEmpty => children.find(node => node.x == x && node.y == y)
      case noChildren => None
    }


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
        cache += parentVertex.getIdentity -> parentNode
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
        cache += childVertex.getIdentity -> childNode
        reloadAnd(childVertex, persisted){
          childVertex.addEdge("Parent", persisted)
        }
        newNode(childNode)
      case None => Unexpected.state(s"Creating child node from not persisted: $this")
    }


  //
  // Representation
  //

  override def isChildOf(distantParent: zooming.Node): Boolean = if (checkParentsConsistency) distantParent match {
    case parent: Node if isPersisted && parent.isPersisted =>
      val sql =
        s"""
        |SELECT COUNT(*) AS c FROM (
        |  TRAVERSE out_Parent
        |  FROM ${persisted.get.getIdentity}
        |  WHILE true
        |) WHERE @rid = ${parent.persisted.get.getIdentity}
      """.stripMargin
      val rows = select(sql)
      if (rows.iterator().hasNext) rows.iterator().next().field[Long]("c") > 0 else false
    case _ => true
  } else true

  def representations(creator: Creator)(implicit cache: R.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    persisted match {
      case None => EmptyRepresentationStorageIterable
      case Some(persisted) => edges("in_Inside") map { edge =>
        cache(edge.getIdentity) match {
          case Some(cached) => cached
          case None => Representation.load(persisted.getGraph.getVertex(edge), creator)
        }
      }
    }


  //
  // Miscellaneous
  //


  override def hashCode(): Int = persisted match {
    case Some(p) => p.getIdentity.hashCode()
    case None => super.hashCode()
  }

  override def equals(o: Any): Boolean = o match {
    case n: Node if n.isPersisted && isPersisted => persisted.get.getIdentity == n.persisted.get.getIdentity
    case _ => super.equals(o)
  }

  override def iterator: Iterator[zooming.Node] = children.iterator

  override def toString() = if (isPersisted) s"{ONode: ${persisted.get}}" else "{Node: x=" + x + ", y=" + y + "}"
}

object Node extends DefaultCache[Node] {
  type Rows = jl.Iterable[ODocument]

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
    iterableAsScalaIterable(rows) flatMap { row => try {
      val record = row.field[ODocument](field)
      cache(record.getIdentity) match {
        case node: Some[Node] => node
        case None => try {
          val loadedNode = new Node(record.field[Byte]("x"), record.field[Byte]("y"), cache)
          val loadedVertex = storage.getVertex(record)
          loadedNode.persisted = loadedVertex
          cache += loadedVertex.getIdentity -> loadedNode
          val rez = Some(cache(record.getIdentity))
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
