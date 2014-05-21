package com.auginte.desktop

import com.auginte.desktop.operations.{AddWithMouse}
import scalafx.Includes._
import com.auginte.desktop.rich.RichSPane
import javafx.scene.layout.{Pane => jp}
import javafx.scene.Node
import com.auginte.desktop.actors.{DragableView, Container}
import com.auginte.desktop.zooming.ZoomableCamera

/**
 * JavaFX panel with Infinity zooming layout.
 * Can share content with over Views.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends RichSPane
with Container[jp] with DragableView[jp] with ZoomableCamera {
  val contextMenu = initContextMenu()

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    contextMenu.layoutX <== (width - contextMenu.width) / 2
    contextMenu.layoutY <== height - contextMenu.height
    content += contextMenu
    contextMenu.hide()
    contextMenu
  }

  def remove(element: Node): Unit = content.remove(element)
}