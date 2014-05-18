package com.auginte.desktop.operations

import javafx.{collections => jfxc}
import javafx.{scene => jfxs}
import com.auginte.desktop.nodes.Label
import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.{scene => jfxs}
import scalafx.scene.input.MouseEvent
import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.{scene => jfxs}
import com.auginte.desktop.rich.RichNode

/**
 * Simple functionality to add new elements on cursor position
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait AddWithMouse[D <: jfxs.Node] extends RichNode[D] {
  def content: jfxc.ObservableList[jfxs.Node]

  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1) insertElement(createNewElement, e.x, e.y)
  }

  protected def createNewElement: jfxs.Node = new Label("Test")

  protected def insertElement(element: jfxs.Node, x: Double, y: Double): Unit = {
    element.setTranslateX(x)
    element.setTranslateY(y)
    content.add(element)
  }
}
