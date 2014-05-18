package com.auginte.desktop.actors

import com.auginte.desktop.operations.AddWithMouse
import javafx.{scene => jfxs}
import javafx.scene.Node
import com.auginte.desktop.events.InsertElement

/**
 * Delegating insert events to View actor.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Container[D <: jfxs.Node] extends ViewableNode with AddWithMouse[D]  {
  override protected def insertElement(element: Node, x: Double, y: Double): Unit = {
    view ! InsertElement(element, x, y)
  }
}