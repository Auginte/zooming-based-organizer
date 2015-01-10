package com.auginte.desktop.operations

import com.auginte.desktop.HaveOperations

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox

/**
 * Alternative to [[javafx.scene.control.ContextMenu]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class ContextMenu extends HBox {

  stylesheets add "css/contextMenu.css"
  styleClass.add("contextMenu")

  val closeButton = new Button("X") {
    onAction = (e: ActionEvent) => ContextMenu.this.visible = false
    stylesheets add "css/contextMenu.css"
    styleClass.add("contextMenuButton")
  }

  content = Seq(closeButton)

  def show(): Unit = this.synchronized(visible = true)

  def showContent(elements: HaveOperations#Operations): Unit = {
    content = for (element <- elements) yield new Button(element._1) {
      onAction = (e: ActionEvent) => {
        element._2(e)
        hide()
      }
      stylesheets add "css/contextMenu.css"
      styleClass.add("contextMenuButton")
    }
    content += closeButton
    content.get(0).requestFocus()
  }

  def hide(): Unit = this.synchronized(visible = false)

  prefHeight = 20
}
