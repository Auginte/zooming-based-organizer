package com.auginte.desktop.operations

/**
 * Operations related to zooming/scaling element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Zoom {
  protected def zoomed(scale: Double, x: Double, y: Double)

  @inline
  protected final def delta2scale(delta: Double) = 1 + (1 / delta)
}
