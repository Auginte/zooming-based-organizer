package com.auginte.distribution

import com.auginte.common.Unexpected
import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.orientechnologies.orient.core.record.impl.ODocument
import java.{lang => jl}
import com.tinkerpop.blueprints.Element

import scala.collection.JavaConversions._

import com.tinkerpop.blueprints.impls.orient.{OrientElementIterable, OrientBaseGraph, OrientVertex}

import scala.language.implicitConversions

/**
 * Persisting data to OreintDb database.
 *
 * Using 3 layer architecture:
 * {{{
 *   View objects, which can share same data
 *   Memory data storage
 *   OrientDB document for persistence and complex relations
 * }}}
 *
 * So it would be easy to go from view object to database and vise versa.
 *
 *
 * For `View objects`, using [[com.auginte.distribution.orientdb.RepresentationWrapper]]
 * For `Memory data storage`, using [[com.auginte.distribution.orientdb.Persistable]]
 * For storage in database, using [[com.orientechnologies.orient.core.record.impl.ODocument]]
 * For faster relations, using [[com.auginte.distribution.orientdb.Cache]]
 */
package object orientdb {

  private[orientdb] val EmptyDocumentIterable = emptyIterable[ODocument]

  private[orientdb] val EmptyRepresentationStorageIterable = emptyIterable[RepresentationWrapper]

  private[orientdb] val EmptyNodeIterable = emptyIterable[Node]

  private def emptyIterable[A] = new Iterable[A] {
    override def iterator = new Iterator[A] {
      override def hasNext: Boolean = false

      override def next(): A = throw new RuntimeException("Empty iterator has no elements")
    }
  }

  private[orientdb] def proxyIterable[A, B](source: jl.Iterable[A], f: A => B) = new Iterable[B] {
    override def iterator: Iterator[B] = new Iterator[B] {
      val original = source.iterator()

      override def hasNext: Boolean = original.hasNext

      override def next(): B = f(original.next())
    }
  }

  val reThrow = (e: Exception) => throw e

  val defaultRetryCount = 10

  def reloadAnd(vertexes: OrientVertex*)(f: => Unit)(implicit retry: Int = defaultRetryCount): Unit = try {
    vertexes.foreach(_.reload())
    f
  } catch {
    case e: OConcurrentModificationException if retry > 0 => reloadAnd(vertexes: _*)(f)(retry - 1)
  }

  def inTransaction(db: OrientBaseGraph)(databaseActions: => Unit)(implicit solve: Exception => Unit = reThrow): Unit = {
    val transaction = db.getRawGraph.getTransaction
    transaction.begin()
    try {
      databaseActions
      transaction.commit(true)
    } catch {
      case e: Exception =>
        transaction.rollback()
        solve(e)
    }
  }

  def duplicatedRecord(source: OrientVertex)(implicit ignoreEdges: List[String] = List("Refer")): OrientVertex = {
    def duplicateSimpleFields(parameters: Array[List[Object]]) = {
      val className = source.getProperty[String]("@class")
      source.getGraph.addVertex(s"class:$className", parameters.flatten: _*)
    }

    def withEdges(target: OrientVertex, parameters: Array[List[Object]]): OrientVertex = {
      val edge = "(.+)_(.+)".r
      parameters.foreach {
        case List(key: String, edges: OrientElementIterable[_]) => key match {
          case edge("out", name) if !ignoreEdges.contains(name) =>
            toIterable(edges).foreach(linked => target.addEdge(name, linked))
          case edge("in", name) if !ignoreEdges.contains(name)=>
            toIterable(edges).foreach(linked => linked.addEdge(name, target))
          case _ => Unexpected.state(s"Not in-out edge: $key: $edges")
        }
        case other => Unexpected.state(s"Not complex property: $other")
      }
      target
    }

    val ignoredFields = ignoreEdges.map(name => List(s"in_$name", s"out_$name")).flatten
    val usefulFields = source.getRecord.fieldNames().filterNot(ignoredFields.contains)
    val allParameters = usefulFields.map(key => List(key, source.getProperty[Object](key)))
    val groupedParameters = allParameters.groupBy(_(1) match {
      case edges: OrientElementIterable[_] => 'complex
      case properties => 'simple
    })
    val duplicatedVertex = duplicateSimpleFields(groupedParameters('simple))
    withEdges(duplicatedVertex, groupedParameters('complex))
  }

  private def toIterable(i: OrientElementIterable[_]): Iterable[OrientVertex] =
    new Iterable[OrientVertex] {
      override def iterator: Iterator[OrientVertex] = i.iterator().flatMap {
        case v: OrientVertex => Some(v)
        case _ => None
      }
    }


  def debugFields(record: ODocument): String =
    record.fieldNames().map(name => s"$name=${record.field(name)}").mkString(s"{$record>", "\t|\t", "}")
}
