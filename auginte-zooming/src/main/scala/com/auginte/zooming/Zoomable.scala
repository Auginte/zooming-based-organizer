package com.auginte.zooming

/**
 * Interface for elements in infinity grid
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Zoomable {
  def node: Node

  def position: Distance
}
