package com.auginte.desktop.actors

import com.auginte.desktop.operations.{EditableNode, AddWithKeyboard, AddWithMouse}
import javafx.{scene => jfxs}
import javafx.scene.Node
import com.auginte.desktop.events.{EditElement, InsertElement}

/**
 * Delegating insert events to View actor.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Container[D <: jfxs.Node] extends ViewableNode with AddWithMouse[D] with AddWithKeyboard[D] {
  override protected def insertElement(element: Node, x: Double, y: Double): Unit = {
    view ! InsertElement(element, x, y)
    element match {
      case e: EditableNode => view ! EditElement(e, mode=true)
      case _ => Unit
    }
  }
}