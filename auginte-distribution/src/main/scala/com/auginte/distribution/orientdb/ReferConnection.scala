package com.auginte.distribution.orientdb

case class ReferConnection(from: RepresentationWrapper, to: RepresentationWrapper, distance: Int) extends RepresentationWrapper {
  override def storage: Representation = to.storage

  override def cloned: SelfRepresentation = to
}
