package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.auginte.zooming.{Node, Grid}
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}
import scala.collection.mutable

import scala.collection.JavaConversions._

/**
 * Load, merge and save infinity zooming structures
 */
object Position {
  val nodeSeparator = "|"
  val propertySeparator = ","

  def store(grid: Grid, db: OrientBaseGraph): Unit = {
    val newVertex = (x: Int, y: Int) => db.addVertex("class:Node", "x", Byte.box(x.toByte), "y", Byte.box(y.toByte))
    val nodes = grid.flatten
    val node2vertex: Map[Node, OrientVertex] = nodes.map(node => node -> newVertex(node.x, node.y)).toMap
    for (parent <- nodes; child <- parent.children) {
      node2vertex(child).addEdge("Parent", node2vertex(parent))
    }
  }

  def load(db: OrientBaseGraph, grid: Grid): Unit = {
    val noParentRID = ""
    val levelDown = 1.0 / grid.gridSize
    def getRids(path: String): (String, String) = {
      val elements = path.split("\\.in_Parent\\[\\d+\\]").map(inBrackets).reverse
      val current = if (elements.nonEmpty) elements.head else noParentRID
      val parent = if (elements.nonEmpty && elements.tail.nonEmpty) elements.tail.head else noParentRID
      (current, parent)
    }
    val traverseNodesQuery = new OCommandSQL(
      """
        |SELECT $path, x, y FROM (
        |   TRAVERSE in_Parent
        |   FROM (SELECT FROM Node where out_Parent IS NULL LIMIT 1)
        |   WHILE true
        |)
      """.stripMargin)
    val nodes = db.command(traverseNodesQuery).execute[jl.Iterable[Vertex]]()
    if (nodes.nonEmpty) {
      val rid2node = new mutable.HashMap[String, Node]()
      for ((v, key) <- nodes.zipWithIndex) {
        val (currentRID, parentRID) = getRids(v.getProperty[String]("$path"))
        if (parentRID == noParentRID) {
          rid2node.put(currentRID, grid.root)
        } else {
          val parent = rid2node(parentRID)
          val child = grid.getNode(parent, v.getProperty[Byte]("x"), v.getProperty[Byte]("y"), levelDown)
          rid2node.put(currentRID, child)
        }
      }
    }
  }

  def absoluteIds(positions: Grid): Map[String, Node] = {
    val node2pos = new mutable.HashMap[Node, String]()
    val nodes = positions.flatten
    nodes.foreach(n => node2pos.put(n, position(n)))
    Map(nodes.map(n => parents2absoluteIds(n) -> n): _*)
  }

  def absoluteIds(storage: OrientBaseGraph): Map[String, ORID] = {
    val rid2pos = new mutable.HashMap[String, String]()
    val id2rid = new mutable.HashMap[String, ORID]()
    val query = new OCommandSQL(
      """
        |SELECT @rid, $path, x, y FROM (
        |   TRAVERSE in_Parent
        |   FROM (SELECT FROM Node where out_Parent IS NULL LIMIT 1)
        |   WHILE true
        |)
      """.stripMargin)
    for (v <- storage.command(query).execute[jl.Iterable[Vertex]]()) {
      val rid = v.getProperty[OrientVertex]("rid").getIdentity
      rid2pos.put(rid.toString, position(v))
      id2rid.put(path2absoluteIds(v.getProperty("$path"), rid2pos), rid)
    }
    Map(id2rid.toSeq: _*)
  }

  private[orientdb] def path2absoluteIds(path: String, rid2pos: collection.Map[String, String]): String =
    path.split("\\.in_Parent\\[\\d+\\]").map(inBrackets).map(rid2pos).mkString(nodeSeparator)

  private[orientdb] def parents2absoluteIds(node: Node): String = {
    def absoluteIds(node: Node, result: String): String = node.parent match {
      case Some(n) => absoluteIds(n, position(n) + nodeSeparator + result)
      case None => result
    }
    absoluteIds(node, position(node))
  }

  private def position(n: Node): String = n.x + propertySeparator + n.y

  private def position(v: Vertex): String = v.getProperty[Byte]("x") + propertySeparator + v.getProperty[Byte]("y")

  private def inBrackets(text: String) = text.substring(1, text.length - 1)
}
