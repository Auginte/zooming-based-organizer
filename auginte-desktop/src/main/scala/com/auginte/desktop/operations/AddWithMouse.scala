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
trait AddWithMouse[D <: jfxs.Node] extends RichNode[D] with InsertingElements[D] {
  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1) insertElement(createNewElement, e.x, e.y)
  }
}
