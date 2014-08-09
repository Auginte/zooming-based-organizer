package com.auginte.desktop.operations

import javafx.{scene => jfxs}

import scalafx.scene.input.MouseEvent

/**
 * Functionality to move element with mouse.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseMove2D[D <: jfxs.Node] extends Move2D[D] {
  private var beingDragged: Boolean = false
  private var dragX: Double = 0
  private var dragY: Double = 0

  mousePressed += beginDrag

  mouseReleased += finishDrag

  mouseDragged += ((e: MouseEvent) => if (beingDragged) {
    finishDrag(e)
    beginDrag(e)
  })

  private def beginDrag(e: MouseEvent): Unit = {
    dragX = e.screenX
    dragY = e.screenY
    beingDragged = true
    e.consume()
  }

  private def finishDrag(e: MouseEvent): Unit = {
    saveDraggedPosition(e.screenX - dragX, e.screenY - dragY)
    beingDragged = false
    e.consume()
  }
}
