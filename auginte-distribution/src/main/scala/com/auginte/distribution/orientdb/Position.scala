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
