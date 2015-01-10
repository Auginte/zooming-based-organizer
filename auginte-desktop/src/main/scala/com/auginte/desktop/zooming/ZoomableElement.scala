package com.auginte.desktop.zooming

import com.auginte.zooming.Coordinates

/**
 * General purpose element with infinity zooming parameters.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableElement extends UsingGrid {
  /**
   * Absolute coordinates and scaling.
   */
  var position: Coordinates = new Coordinates()

  @inline
  private[zooming] def t = position

  /**
   * Updates absolute coordinates.
   *
   * @param x absolute translation (difference of position)
   * @param y absolute translation (difference of position)
   * @return updated absolute coordinates
   */
  def translate(x: Double, y: Double): Coordinates = {
    position = t.translated(t.x + x, t.y + y)
    position
  }
}
