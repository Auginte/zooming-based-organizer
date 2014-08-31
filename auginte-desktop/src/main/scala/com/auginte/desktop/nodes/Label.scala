package com.auginte.desktop.nodes

import javafx.scene.layout.{Pane => jp}
import javafx.scene.{control => jfxc, layout => jfxl, text => jfxt}

import com.auginte.desktop.actors.{DragableNode, ScalableElement, ViewableNode}
import com.auginte.desktop.events._
import com.auginte.desktop.operations.EditableNode
import com.auginte.desktop.rich.RichJPane
import com.auginte.desktop.zooming.ZoomableNode
import com.auginte.desktop.{HaveOperations, actors => act}
import com.auginte.distribution.data.Data

import scalafx.event.ActionEvent
import scalafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}
import scalafx.scene.{control => sfxc}

/**
 * Editable Label
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Label(val _text: String) extends RichJPane
with ViewableNode with HaveOperations with DragableNode[jp] with ZoomableNode[jp] with ScalableElement[jp]
with Data
with EditableNode {
  private val label = new jfxc.Label(_text)
  private val textArea = new jfxc.TextArea()
  private val enter = util.Properties.lineSeparator
  textArea.setVisible(false)
  textArea.setText(_text)
  getChildren.addAll(label, textArea)
  updateSize(_text)
  private var editMode = false
  updateStyle(editMode)


  //
  // Listeners
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1) {
      editable = !editable
      e.consume()
      textArea.requestFocus()
    } else if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
      e.consume()
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
    (e: KeyEvent) => {
      e.code match {
        case KeyCode.ENTER if e.shiftDown => insertEnter()
        case KeyCode.ENTER if !e.shiftDown => finishEditing(e)
        case _ => Unit
      }
      e.consume()
    }
  )

  def this() = this("")


  //
  // Data
  //

  override val dataType: String = "Text"

  override val storageFields = Map("ag:Text/text" -> (() => text))

  //
  // Operations
  //

  def operations = Map(
    "Edit" -> ((e: ActionEvent) => editable = true),
    "Delete" -> ((e: ActionEvent) => view ! DeleteElement(this))
  )

  def editable = editMode


  //
  // Edit/view mode
  //

  def editable_=(value: Boolean): Unit = {
    editMode = value
    label.setVisible(!editMode)
    textArea.setVisible(editMode)
    updateText()
    updateStyle(editMode)
    if (editMode) textArea.requestFocus()
  }

  private def updateStyle(editable: Boolean): Unit = if (editable) {
    styleClass.remove("label-active")
    styleClass.add("label-editable")
  } else {
    styleClass.add("label-active")
    styleClass.remove("label-editable")
  }

  //
  // Common functions
  //

  override def toString: String = "LABEL: " + text + "\t" + position + "\t" + node.selfAndParents.reverse

  def text: String = if (editable) textArea.getText else label.getText

  def text_=(value: String): Unit = updateText(value)

  //
  // Utilities
  //

  private def insertEnter(): Unit = {
    textArea.insertText(textArea.getCaretPosition, enter)
  }

  private def finishEditing(e: KeyEvent): Unit = {
    e.consume()
    editable = false
    updateText()
    view ! EditElement(this, mode=false)
  }

  private def updateText(text: String = textArea.getText): Unit = {
    label.setText(text.trim)
  }

  private def updateSize(newValue: String): Unit = {
    val string = if (newValue.length > 0) newValue else "i"
    val textObject = new jfxt.Text(string)
    textObject.snapshot(null, null)
    prefWidth = if (newValue.length > 0) textObject.getLayoutBounds.getWidth + 16 else InitialSize.width
    prefHeight = if (newValue.length > 0) textObject.getLayoutBounds.getHeight + 16 else InitialSize.height
    label.setPrefWidth(prefWidth.get)
    label.setPrefHeight(prefHeight.get)
    textArea.setPrefWidth(prefWidth.get)
    textArea.setPrefHeight(prefHeight.get)
    if (editMode) {
      view ! ElementUpdated(this)
    }
  }
}