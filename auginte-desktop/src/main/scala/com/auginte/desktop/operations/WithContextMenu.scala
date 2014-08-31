package com.auginte.desktop.operations

import com.auginte.desktop.HaveOperations
import com.auginte.desktop.actors.{Container, ViewableNode}
import com.auginte.desktop.events.ShowContextMenu
import javafx.scene.{layout => jfxl}

import scalafx.scene.input.{MouseButton, MouseEvent}


/**
 * Functionality for elements, showing context menu
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait WithContextMenu[D <: jfxl.Pane] extends Container[D]
with HaveOperations with ViewableNode  {

  lazy val contextMenu: ContextMenu = initContextMenu()

  private lazy val panelWithContextMenu: Boolean = addContextMenu()

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY && panelWithContextMenu) {
      view ! ShowContextMenu(this)
    }
  }

  protected def addContextMenu(): Boolean = d.getChildren.add(contextMenu)

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    layoutContextMenu(contextMenu)
    contextMenu.hide()
    contextMenu
  }

  protected def layoutContextMenu(menu: ContextMenu): Unit
}
