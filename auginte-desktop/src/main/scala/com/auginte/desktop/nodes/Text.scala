package com.auginte.desktop.nodes

import javafx.scene.layout.{Pane => jp}
import javafx.scene.{text => jfxt, control => jfxc}

import com.auginte.desktop.persistable.ViewWrapper
import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.{GlobalCoordinatesWrapper, RepresentationWrapper}
import com.auginte.distribution.{orientdb => o}
import com.auginte.desktop.persistable

import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}

/**
 * Representing plain text field
 */
class Text extends Node
with RepresentationWrapper with ViewWrapper
with persistable.MouseMove2D[jp] with persistable.MouseScale[jp]
{
  private var _data = new o.Text()

  override def storage: o.Text = _data

  def storage_=(data: o.Text): Unit = _data = data

  storage.text = ""

  private val label = new jfxc.Label(storage.text)
  private val textArea = new jfxc.TextArea()
  private val enter = util.Properties.lineSeparator
  textArea.setVisible(false)
  textArea.setText(storage.text)
  getChildren.addAll(label, textArea)
  updateSize(storage.text)
  private var editMode = false
  updateStyle(editMode)


  //
  // Style
  //

  getStyleClass add "label"
  label.getStyleClass add "label"
  textArea.getStyleClass add "textArea"
  textArea.setLayoutY(3)
  textArea.setLayoutX(-7)


  //
  // Listeners
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1) {
      editable = !editable
      e.consume()
      textArea.requestFocus()
    } else if (e.button == MouseButton.SECONDARY) {
      e.consume()
    }
  }

  textArea.textProperty().addListener((s: String) => updateSize(s))

  textArea.addEventFilter(
    KeyEvent.KeyPressed,
    (e: KeyEvent) => e.code match {
      case KeyCode.ENTER => e.consume()
      case KeyCode.SPACE => e.consume()
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


  //
  // Transformation
  //

  //  override protected def createCloned(): Unit = {
  //    val clone = transformed(cloneParameters).original
  //    clone.swapSources(this)
  //    view ! ImportElement(clone)
  //  }

  //  override protected def copy: Label = {
  //    val label = new Label(text)
  //    label.node = node
  //    label.position = position.clone()
  //    label
  //  }

  //
  // Operations
  //

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

  override def toString: String = "LABEL: " + text + "\t" + storage

  def text: String = if (editable) textArea.getText else storage.text

  def text_=(value: String): Unit = updateText(value)

  //
  // Persistence
  //

  override def updateDbToCached(): Unit = storage.persisted match {
    case Some(p) =>
      textArea.setText(storage.text)
      updateText(storage.text)
    case None => Unit
  }

  override def updateCachedToDb(): Unit = {
    storage.text = textArea.getText
    storage.scale = this.getScaleZ
    storage.save()
  }



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
  }

  private def updateText(text: String = textArea.getText): Unit = {
    label.setText(text.trim)
    storage.text = text.trim
    storage.save()
  }

  private def updateSize(newValue: String): Unit = Platform.runLater {
    val string = if (newValue.length > 0) newValue else "i"
    val textObject = new jfxt.Text(string)
    textObject.snapshot(null, null)
    prefWidth = if (newValue.length > 0) textObject.getLayoutBounds.getWidth + 16 else InitialSize.width
    prefHeight = if (newValue.length > 0) textObject.getLayoutBounds.getHeight + 16 else InitialSize.height
    label.setPrefWidth(prefWidth.get)
    label.setPrefHeight(prefHeight.get)
    textArea.setPrefWidth(prefWidth.get)
    textArea.setPrefHeight(prefHeight.get)
  }
}
