package com.auginte.desktop.actors

import com.auginte.desktop.operations.MouseMove2D
import javafx.{scene => jfxs}
import com.auginte.desktop.events.MoveElement
import com.auginte.desktop.zooming.ZoomableNode

/**
 * Delegating every drag event to View as Event
 *
 * So dragging could be viewed in other Views, computers or saved to history.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait DragableNode[D <: jfxs.Node] extends ViewableNode
with MouseMove2D[D] with ZoomableNode[D] {
  override def saveDraggedPosition(diffX: Double, diffY: Double): Unit = {
    view ! MoveElement(d, this, diffX, diffY)
  }
}

