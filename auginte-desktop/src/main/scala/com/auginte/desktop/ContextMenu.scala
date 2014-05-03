package com.auginte.desktop

import scalafx.scene.layout.{HBox, Pane}
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.Includes._
import java.util.concurrent.atomic.AtomicBoolean
import javafx.scene.Node
import javafx.collections.ObservableList

/**
 * Alternative to [[javafx.scene.control.ContextMenu]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class ContextMenu extends HBox {

  stylesheets add "css/contextMenu.css"
  styleClass.add("contextMenu")

  val closeButton = new Button("X") {
    onAction = ((e: ActionEvent) => ContextMenu.this.visible = false)
  }

  content = Seq(closeButton)

  def show(): Unit = this.synchronized(visible = true)

  def showContent(elements: HaveOperations#Operations): Unit = {
    content = for (element <- elements) yield new Button(element._1) {
      onAction = (e: ActionEvent) => {
        element._2(e)
        hide()
      }
    }
    content += closeButton
    content.get(0).requestFocus()
  }

  def hide(): Unit = this.synchronized(visible = false)

  prefHeight = 20
}
