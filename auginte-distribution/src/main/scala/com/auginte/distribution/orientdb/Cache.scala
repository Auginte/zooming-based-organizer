package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.impl.ODocument

/**
 * Functionality to cache relation between fetched OrientDB and Wrapper classes.
 *
 * Dealing with concurrency problems by exposing immutable map,
 * but simulating mutable behavior by changing reverence internal reference.
 */
class Cache[A] {
  private var _map: Map[ORID, A] = Map()

  def apply(key: ORID): Option[A] = _map.get(key)

  def apply(keys: Iterable[ODocument]): Iterable[A] = keys.flatMap(key => _map.get(key.getIdentity))

  def apply(key: Option[ODocument]): Option[A] = key match {
    case Some(k) => apply(k.getIdentity)
    case _ => None
  }

  def get = _map

  def contains(document: ODocument) = _map.contains(document.getIdentity)

  def size = _map.size

  def +=(element: (ORID, A)): Unit = _map = _map + element

  def -=(document: ORID): Unit = _map = _map - document

  def -=(wrapper: A): Unit = _map = _map.filterNot(_._2 equals wrapper)

  private[auginte] def clear() = _map = Map()
}
