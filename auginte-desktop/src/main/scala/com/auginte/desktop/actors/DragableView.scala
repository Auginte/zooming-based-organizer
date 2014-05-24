package com.auginte.desktop.actors

import com.auginte.desktop.operations.MouseMove2D
import javafx.scene.{layout => jfxl}
import com.auginte.desktop.zooming.ZoomableCamera
import com.auginte.desktop.events.MoveView

/**
 * Delegating every view drag event to View as Event
 *
 * So dragging could be viewed in other Views, computers or saved to history.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait DragableView[D <: jfxl.Pane] extends ViewableNode
with MouseMove2D[D] with ZoomableCamera[D] {

  override protected def saveDraggedPosition(diffX: Double, diffY: Double): Unit = {
    view ! MoveView(this, diffX, diffY)
  }
}

