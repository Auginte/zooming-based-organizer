package com.auginte.desktop.operations

import com.auginte.desktop.HaveOperations
import com.auginte.desktop.actors.{Container, ViewableNode}
import com.auginte.desktop.events.ShowContextMenu
import javafx.scene.{layout => jfxl}

import scalafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}


/**
 * Functionality for elements, showing context menu
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait WithContextMenu[D <: jfxl.Pane] extends Container[D]
with HaveOperations with ViewableNode  {

  lazy val contextMenu: ContextMenu = initContextMenu()

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
    }
  }

  keyPressed += {
    (e: KeyEvent) => if (isContextMenuKey(e.code)) {
      view ! ShowContextMenu(this)
    }
  }

  protected def isContextMenuKey(code: KeyCode): Boolean = code == KeyCode.SPACE || code == KeyCode.F1

  protected def addContextMenu(): Unit = if (!d.getChildren.contains(contextMenu)) d.getChildren.add(contextMenu)

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    layoutContextMenu(contextMenu)
    contextMenu.hide()
    contextMenu
  }

  protected def layoutContextMenu(menu: ContextMenu): Unit
}
