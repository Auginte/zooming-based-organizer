package com.auginte.desktop.persistable

import javafx.scene.{layout => jfxl}

import com.auginte.desktop.operations.KeyboardMove2D
import com.auginte.desktop.operations

import scalafx.scene.input.MouseEvent

/**
 * Delegating every drag view to camera
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait DragableView[D <: jfxl.Pane] extends View
with operations.MouseMove2D[D] with KeyboardMove2D[D] {
  protected val keyboardDragStep = -15.0

  override def saveDraggedPosition(diffX: Double, diffY: Double): Unit = camera match {
    case Some(c) =>
      translate(diffX, diffY)
      updateCachedToDb()
    case None => Unit
  }

  override protected def isMouseMoveCondition(e: MouseEvent): Boolean =
    delegatedJavaFxView == e.target && super.isMouseMoveCondition(e)

  @inline
  private final def delegatedJavaFxView = d
}

