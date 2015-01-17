package com.auginte.desktop.operations

import javafx.{scene => jfxs}

import com.auginte.desktop.rich.RichJPane

import scalafx.scene.input.MouseEvent

/**
 * Functionality to move other element with mouse.
 * 
 * Useful, when moving is needed of newly created element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseMoveElement2D[D <: jfxs.Node] extends Move2D[D] {
  type Draggable = RichJPane with Move2D[_]

  private var dragging: Option[Draggable] = None
  private var dragX: Double = 0
  private var dragY: Double = 0

  mousePressed += beginDrag

  mouseReleased += endElementDrag

  mouseDragged += ((e: MouseEvent) => if (elementBeingDragged) {
    endDrag(e)
    beginDrag(e)
  })

  
  protected final def beginElementDrag(element: Draggable, e: MouseEvent): Unit = {
    dragging = Some(element)
    beginDrag(e)
  }

  protected final def endElementDrag(e: MouseEvent): Unit = {
    endDrag(e)
    dragging = None
  }

  
  private def beginDrag(e: MouseEvent): Unit = if (isElementMouseMoveCondition(e)) {
    dragX = e.screenX
    dragY = e.screenY
    e.consume()
  }

  private def endDrag(e: MouseEvent): Unit = dragging match {
    case Some(element) =>
      element.saveDraggedPosition(e.screenX - dragX, e.screenY - dragY)
      e.consume()
    case _ => Unit
  }
  
  
  protected final def elementBeingDragged: Boolean = dragging.isDefined

  protected def isElementMouseMoveCondition(e: MouseEvent): Boolean = true
}
