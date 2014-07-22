package com.auginte.desktop.operations

import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}
import scalafx.scene.input.ScrollEvent

/**
 * Functionality to zoom elements with mouse
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseZoom[D <: jfxs.Node] extends RichNode[D] {

  scrolled += ((e: ScrollEvent) => zoomed(delta2scale(e.deltaY), e.x, e.y))

  @inline
  private def delta2scale(delta: Double) = 1 + (1 / delta)

  protected def zoomed(scale: Double, x: Double, y: Double)
}
