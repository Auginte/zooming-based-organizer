package com.auginte.distribution.orientdb

/**
 * Wrapper for data storage, so multiple GUI elements could use same data.
 * So there are 3 layers: GUI <-> storage <-> Database
 */
trait RepresentationWrapper
  extends DelayedPersistence
  with ReferWrapper[Representation, RepresentationWrapper] {

  def storage: Representation
}

object RepresentationWrapper {
  private[auginte] def apply(f: => Representation): RepresentationWrapper = withValue(f)

  private[orientdb] def withValue(value: Representation): RepresentationWrapper = new RepresentationWrapper {
    override def storage: Representation = value

    def cloned: RepresentationWrapper = withValue(value.clone())
  }
}