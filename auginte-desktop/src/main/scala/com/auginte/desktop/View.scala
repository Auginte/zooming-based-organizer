package com.auginte.desktop

import scalafx.scene.input.{ScrollEvent, MouseEvent}
import com.auginte.desktop.operations.{AddWithMouse}
import scalafx.Includes._
import com.auginte.desktop.rich.RichSPane
import javafx.scene.layout.{Pane => jp}
import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi, control => jfxc, Node}
import javafx.{scene => jfxs}
import com.auginte.desktop.actors.Container

/**
 * JavaFX panel with Infinity zooming layout.
 * Can share content with over Views.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends RichSPane with Container[jp]  {
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