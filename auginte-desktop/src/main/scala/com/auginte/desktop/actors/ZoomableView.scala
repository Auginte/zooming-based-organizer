package com.auginte.desktop.actors

import com.auginte.desktop.operations.{KeyboardZoom, MouseZoom, MouseMove2D}
import com.auginte.desktop.zooming.ZoomableNode
import javafx.scene.{layout => jfxl}
import com.auginte.desktop.events.ZoomView

/**
 * Delegating every zoom event to View as event
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableView[D <: jfxl.Pane] extends ViewableNode
with MouseZoom[D] with KeyboardZoom[D] with ZoomableNode[D] {
  override protected def zoomed(scale: Double, x: Double, y: Double): Unit = {
    view ! ZoomView(this, scale, x, y)
  }
}
