package com.auginte.desktop.zooming

import com.auginte.zooming.Distance

/**
 * General purpose element with infinity zooming parameters.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableElement {
  /**
   * Absolute coordinates and scaling.
   */
  var transformation: Distance = new Distance()

  @inline
  private[zooming] def t = transformation

  /**
   * Updates absolute coordinates.
   *
   * @param x absolute translation (difference of position)
   * @param y absolute translation (difference of position)
   * @return updated absolute coordinates
   */
  def translate(x: Double, y: Double): Distance = {
    transformation = t.translated(t.x + x, t.y + y)
    transformation
  }
}
