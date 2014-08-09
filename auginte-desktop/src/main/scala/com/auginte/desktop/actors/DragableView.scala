package com.auginte.desktop.actors

import javafx.scene.{layout => jfxl}

import com.auginte.desktop.events.MoveView
import com.auginte.desktop.operations.{KeyboardMove2D, MouseMove2D}
import com.auginte.desktop.zooming.ZoomableCamera

/**
 * Delegating every view drag event to View as Event
 *
 * So dragging could be viewed in other Views, computers or saved to history.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait DragableView[D <: jfxl.Pane] extends ViewableNode
with MouseMove2D[D] with KeyboardMove2D[D]
with ZoomableCamera[D] {
  protected val keyboardDragStep = -15.0

  override protected def saveDraggedPosition(diffX: Double, diffY: Double): Unit = {
    view ! MoveView(this, diffX, diffY)
  }
}

