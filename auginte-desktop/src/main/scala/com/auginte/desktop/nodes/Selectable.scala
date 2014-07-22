package com.auginte.desktop.nodes

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}
import javafx.collections.ObservableList
import com.auginte.desktop.rich.RichNode

/**
 * Changing selection property via mouse.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Selectable[D <: jfxs.Node] extends RichNode[D] {
    def getStyleClass: ObservableList[String]

    private var _selected = false

    def selected = _selected

    mouseClicked += {
      (event: sfxi.MouseEvent) => if (_selected) {
        _selected = false
        getStyleClass.remove("selected")
      } else {
        _selected = true
        getStyleClass.add("selected")
      }
    }
}
