package com.auginte.desktop.persistable

import javafx.scene.{layout => jfxl}

import com.auginte.common.Unexpected
import com.auginte.desktop.operations
import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.RepresentationWrapper

import scalafx.scene.input.MouseEvent

/**
 * Creating cloned element with reference to the source.
 */
trait MouseTransform extends RichJPane
  with RepresentationWrapper
  with ViewWrapper
  with operations.MouseMove2D[jfxl.Pane]
  with operations.MouseMoveElement2D[jfxl.Pane]
{
  mousePressed += ((e: MouseEvent) => if (!elementBeingDragged && transformationCondition(e)) createTransformed(e))

  override protected def isMouseMoveCondition(e: MouseEvent): Boolean =
    !transformationCondition(e) && super.isMouseMoveCondition(e)

  override protected def isElementMouseMoveCondition(e: MouseEvent): Boolean = transformationCondition(e)

  protected def transformationCondition(e: MouseEvent) = e.shiftDown

  private def createTransformed(e: MouseEvent): Unit = view match {
    case Some(view: Container) => copyLinked() match {
      case guiElement: RichJPane with operations.MouseMove2D[_] =>
        view.add(guiElement)
        beginElementDrag(guiElement, e)
      case _ => Unexpected.state(s"Duplicating linked non GUI representation: ${copyLinked(swap=true)} in $this")
    }
    case other => Unexpected.state(s"Duplicating linked $other without view from $this")
  }
}
