package com.auginte.distribution.data

/**
 * Marker for cameras/views of data.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Camera extends Data

object Camera {
  def apply(id: String) = new Camera {
    override val storageId = id
  }

  def unapply(c: Camera): Option[String] = Some(c.storageId)
}