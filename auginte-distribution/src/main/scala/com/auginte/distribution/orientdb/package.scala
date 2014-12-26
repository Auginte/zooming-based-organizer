package com.auginte.distribution

import com.orientechnologies.orient.core.record.impl.ODocument
import java.{lang => jl}

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
 * For `Memory data storage`, using [[com.auginte.distribution.orientdb.Persistable]]
 * For storage in database, using [[com.orientechnologies.orient.core.record.impl.ODocument]]
 * For faster relations, using [[com.auginte.distribution.orientdb.Cache]]
 */
package object orientdb {

  private[orientdb] val EmptyDocumentIterable = emptyIterable[ODocument]

  private[orientdb] val EmptyRepresentationIterable = emptyIterable[Representation]

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
}
