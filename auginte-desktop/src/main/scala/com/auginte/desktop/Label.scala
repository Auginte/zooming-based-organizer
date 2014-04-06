package com.auginte.desktop

import scalafx.scene.input.MouseEvent
import javafx.scene.{text => jfxt}
import javafx.beans.value.{ObservableValue, ChangeListener}
import scalafx.scene.control.TextArea
import java.lang.Boolean

/**
 * Editable Label
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Label(val _text: String) extends TextArea with RichNode {
  text = _text
  updateSize(_text)
  editable = false
  updateStyle(editable.value)

  mouseClickEvents += {
    (e: MouseEvent) => if (e.clickCount > 1) {
      editable = if (editable.value == true) false else true
    }
  }

  text.addListener(new ChangeListener[String] {
    override def changed(p1: ObservableValue[_ <: String], p2: String, _text: String): Unit = updateSize(_text)
  })

  editable.addListener(new ChangeListener[Boolean] {
    override def changed(a: ObservableValue[_ <: Boolean], b: Boolean, newValue: Boolean): Unit = updateStyle(newValue)
  })

  private def updateSize(newValue: String): Unit = {
    val string = if (newValue.length > 0) newValue else "i"
    val textObject = new jfxt.Text(string)
    textObject.snapshot(null, null)
    prefWidth = textObject.getLayoutBounds.getWidth + 16
    prefHeight = textObject.getLayoutBounds.getHeight + 16
  }

  private def updateStyle(editable: Boolean): Unit = if (editable == true) {
    styleClass.remove("label-active")
    styleClass.add("label-editable")
  } else {
    styleClass.add("label-active")
    styleClass.remove("label-editable")
  }
}
