package com.auginte.desktop.operations

import javafx.{scene => jfxs}
import scalafx.scene.input.MouseEvent

/**
 * Functionality to move element with mouse.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseMove2D[D <: jfxs.Node] extends MouseMoveElement2D[D] {
  private var beingDragged: Boolean = false
  private var dragX: Double = 0
  private var dragY: Double = 0

  mousePressed += beginDrag

  mouseReleased += endDrag

  mouseDragged += ((e: MouseEvent) => if (beingDragged) {
    endDrag(e)
    beginDrag(e)
  })

  private def beginDrag(e: MouseEvent): Unit = if (isMouseMoveCondition(e)) {
    dragX = e.screenX
    dragY = e.screenY
    beingDragged = true
    e.consume()
  }

  private def endDrag(e: MouseEvent): Unit = if (isMouseMoveCondition(e)) {
    saveDraggedPosition(e.screenX - dragX, e.screenY - dragY)
    beingDragged = false
    e.consume()
  }

  protected def isMouseMoveCondition(e: MouseEvent): Boolean = true
}
