package com.auginte.desktop.nodes

import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.{layout => jfxl}
import javafx.scene.{text => jfxt, control => jfxc}
import com.auginte.desktop.{actors => act, HaveOperations}
import scalafx.scene.{control => sfxc}
import com.auginte.desktop.events.{DeleteElement, ShowContextMenu}
import com.auginte.desktop.actors.{DragableNode, ViewableNode}
import com.auginte.desktop.rich.RichJPane
import javafx.scene.input.MouseButton
import scalafx.geometry.Pos
import scalafx.event.ActionEvent
import com.auginte.desktop.zooming.ZoomableNode
import javafx.scene.layout.{Pane => jp}

/**
 * Editable Label
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Label(val _text: String) extends RichJPane
with ViewableNode with HaveOperations with DragableNode[jp] with ZoomableNode[jp] {
  private val label = new jfxc.Label(_text)
  private val textArea = new jfxc.TextArea()
  private var editMode = false
  private var insertingEnter = false // insertString triggers Key event
  textArea.setVisible(false)
  textArea.setText(_text)
  getChildren.addAll(label, textArea)
  updateSize(_text)
  updateStyle(editMode)


  //
  // Listeners
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1) {
      editable = if (editable == true) false else true
      e.consume()
      textArea.requestFocus()
    } else if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
    }
  }

  textArea.textProperty().addListener((s: String) => updateSize(s))

  textArea.addEventFilter(
    KeyEvent.KeyPressed,
    (e: KeyEvent) => e.code match {
      case KeyCode.ENTER => e.consume()
      case _ => Unit
    }
  )
  textArea.addEventFilter(
    KeyEvent.KeyReleased,
    (e: KeyEvent) => e.code match {
      case KeyCode.ENTER if e.shiftDown => insertEnter()
      case KeyCode.ENTER if (!e.shiftDown) => finishEditing(e)
      case _ => Unit
    }
  )

  private val enter = util.Properties.lineSeparator

  private def insertEnter(): Unit = {
    textArea.insertText(textArea.getCaretPosition, enter)
  }

  private def finishEditing(e: KeyEvent): Unit = {
    e.consume()
    editable = false
    updateText()
  }


  //
  // Edit/view mode
  //

  def editable = editMode

  def editable_=(value: Boolean): Unit = {
    editMode = value
    label.setVisible(!editMode)
    textArea.setVisible(editMode)
    updateText()
    updateStyle(editMode)
  }


  //
  // Representation
  //

  private def updateText(text: String = textArea.getText): Unit = {
    label.setText(text.trim)
  }

  private def updateSize(newValue: String): Unit = {
    val string = if (newValue.length > 0) newValue else "i"
    val textObject = new jfxt.Text(string)
    textObject.snapshot(null, null)
    prefWidth = textObject.getLayoutBounds.getWidth + 16
    prefHeight = textObject.getLayoutBounds.getHeight + 16
    label.setPrefWidth(prefWidth.get)
    label.setPrefHeight(prefHeight.get)
    textArea.setPrefWidth(prefWidth.get)
    textArea.setPrefHeight(prefHeight.get)
    label.setAlignment(Pos.TOP_LEFT)
  }

  private def updateStyle(editable: Boolean): Unit = if (editable == true) {
    styleClass.remove("label-active")
    styleClass.add("label-editable")
  } else {
    styleClass.add("label-active")
    styleClass.remove("label-editable")
  }


  //
  // Operations
  //

  def operations = Map(
    "Edit" -> ((e: ActionEvent) => editable = true),
    "Delete" -> ((e: ActionEvent) => view ! DeleteElement(this))
  )

  override def toString(): String = "LABEL: " + super.toString()
}
