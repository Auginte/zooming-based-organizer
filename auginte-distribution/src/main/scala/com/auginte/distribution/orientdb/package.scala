package com.auginte.distribution

import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.orientechnologies.orient.core.record.impl.ODocument
import java.{lang => jl}

import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}

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

  def duplicatedRecord(source: OrientVertex): OrientVertex = {
    val className = source.getProperty[String]("@class")
    val parameters = source.getRecord.fieldNames().map { key =>
      List(key, source.getProperty[Object](key))
    }.flatten.toList
    source.getGraph.addVertex(s"class:$className", parameters: _*)
  }

  def debugFields(record: ODocument): String =
    record.fieldNames().map(name => s"$name=${record.field(name)}").mkString(s"{$record>", "\t|\t", "}")
}
