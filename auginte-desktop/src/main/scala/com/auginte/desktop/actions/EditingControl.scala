package com.auginte.desktop.actions

import scalafx.scene.input.MouseEvent
import com.auginte.desktop.RichNode
import javafx.{scene => jfxs}
import javafx.{collections => jfxc}
import javafx.scene.{control => jfxsc}
import scala.collection.JavaConversions._

/**
 * Disables editing of object, when clicked on pane (outside object)
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait EditingControl extends RichNode {
  def content: jfxc.ObservableList[jfxs.Node]

  mouseClickEvents += {
    (event: MouseEvent) => for (element <- content) element match {
      case e: jfxsc.TextArea => {
        e.setEditable(false)
      }
      case _ => Unit
    }
  }
}
