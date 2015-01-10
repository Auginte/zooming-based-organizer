package com.auginte.distribution.orientdb

/**
 * Wrapper for data storage, so multiple GUI elements could use same data.
 * So there are 3 layers: GUI <-> storage <-> Database
 */
trait RepresentationWrapper {
  def storage: Representation
}

object RepresentationWrapper {
  def apply(f: => Representation) = new RepresentationWrapper {
    val saved = f
    override def storage: Representation = saved
  }
}