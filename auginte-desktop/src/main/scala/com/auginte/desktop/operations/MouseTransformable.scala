package com.auginte.desktop.operations

import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}

import scalafx.scene.input.MouseEvent

/**
 * Element, that can be cloned with mouse.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseTransformable[D <: jfxs.Node] extends RichNode[D] {
  private var mouseAlreadyPressed = false

  protected val cloneParameters = Map("type" -> "clone")

  mousePressed += ((e: MouseEvent) => if (!mouseAlreadyPressed && isCloneCondition(e)) {
    createCloned()
    mouseAlreadyPressed = true
  })

  mouseReleased += ((e: MouseEvent) => mouseAlreadyPressed = false)

  protected def isCloneCondition(e: MouseEvent): Boolean = e.shiftDown

  protected def createCloned(): Unit
}