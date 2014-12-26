package com.auginte.distribution.orientdb

/**
 * Having separate cache per each storage type.
 */
trait DefaultCache[T] {
  var defaultCache: Cache[T] = new Cache[T]
}
