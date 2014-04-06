package com.auginte.desktop.actions

import scalafx.scene.input.MouseEvent
import com.auginte.desktop.RichNode
import javafx.{collections => jfxc}
import scalafx.scene.control.Label
import javafx.{scene => jfxs}
import scalafx.Includes._

/**
 * Simple functionality to add new elements on cursor position
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait AddWithMouse extends RichNode {
  def content: jfxc.ObservableList[jfxs.Node]

  mouseEvents += {
    (e: MouseEvent) => if (e.clickCount > 1) {
      val element = new Label("Test")
      element.setTranslateX(e.x)
      element.setTranslateY(e.y)
      content += element
    }
  }
}
