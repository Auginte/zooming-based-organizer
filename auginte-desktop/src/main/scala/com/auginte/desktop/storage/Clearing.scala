package com.auginte.desktop.storage

import javafx.scene.{layout => jfxl}

import com.auginte.desktop.actors.Container
import com.auginte.desktop.operations.WithContextMenu

import scalafx.application.Platform

/**
 * Functionality to clear view without removing context menu.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Clearing[D <: jfxl.Pane] extends Container[D] with WithContextMenu[D] {
  protected def clearElements(): Unit = Platform.runLater {
    d.getChildren.clear()
    withContextMenu()
  }

  private def withContextMenu(): Unit = addContextMenu()
}
