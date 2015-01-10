package com.auginte.desktop.operations

import com.auginte.desktop.HaveOperations
import com.auginte.desktop.actors.Container
import com.auginte.desktop.events.ShowContextMenu
import com.auginte.desktop.rich.RichNode

import scalafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}
import scalafx.scene.{layout => sfxl}
import javafx.scene.{layout => jfxl}

/**
 * Functionality for elements, showing context menu
 *
 * @deprecated Will not depend on Akka. See [[ContextMenuWrapper]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ContextMenuWrapper extends RichNode[jfxl.Pane]
with HaveOperations {
  self: sfxl.Pane =>

  private val contextMenu = new ContextMenu

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) showContextMenu(this)
  }

  keyPressed += {
    (e: KeyEvent) => if (isContextMenuKey(e.code)) showContextMenu(this)
  }


  def showContextMenu(source: HaveOperations): Unit = {
    if (!content.contains(contextMenu)) {
      content.add(contextMenu)
    }
    layoutContextMenu(contextMenu)
    contextMenu.showContent(source.operations)
    contextMenu.show()
  }

  protected def isContextMenuKey(code: KeyCode): Boolean = code == KeyCode.SPACE || code == KeyCode.F1

  protected def layoutContextMenu(menu: ContextMenu): Unit = {
    menu.layoutX <== (width - menu.width) / 2
    menu.layoutY <== height - menu.height
  }
}
