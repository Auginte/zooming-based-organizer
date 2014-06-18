package com.auginte.desktop.nodes

import java.lang.Boolean
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList
import javafx.scene.{control => jfxc, input => jfxi}
import javafx.{scene => jfxs}
import com.auginte.desktop.rich.RichNode
import scalafx.scene.{input => sfxi}
import scala.language.implicitConversions

/**
 * Disables editing of object, when clicked on pane (outside object)
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseFocusable[D <: jfxs.Node] extends RichNode[D] {
  protected def getStyleClass: ObservableList[String] = d.getStyleClass

  mouseEntered += {
    (event: sfxi.MouseEvent) => {
      d.requestFocus()
    }
  }

  d.focusedProperty().addListener {
    (focused: Boolean) => if (focused) getStyleClass.add("focused") else getStyleClass.remove("focused")
  }


  private implicit def closureToBooleanChangeListener(c: Boolean => Any): ChangeListener[Boolean] =
    closureToChangeListener[Boolean](c)

  private def closureToChangeListener[A](c: A => Any): ChangeListener[A] = new ChangeListener[A] {
    override def changed(observable: ObservableValue[_ <: A], old: A, newValue: A): Unit = c(newValue)
  }
}
