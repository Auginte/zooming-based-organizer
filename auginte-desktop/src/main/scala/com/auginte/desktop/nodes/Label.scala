package com.auginte.desktop.nodes

import com.auginte.desktop.HaveOperations
import com.auginte.desktop.actors.{DragableNode, ScalableElement, ViewableNode}
import com.auginte.desktop.events._
import com.auginte.desktop.operations.{EditableNode, MouseTransformable}
import com.auginte.desktop.rich.RichJPane
import com.auginte.desktop.zooming.ZoomableNode
import com.auginte.distribution.data.Data
import com.auginte.transforamtion.Transformable
import javafx.event.{Event, EventHandler}
import javafx.scene.layout.{Pane => jp}
import javafx.scene.{control => jfxc, text => jfxt}
import scalafx.event.ActionEvent
import scalafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}
import javafx.scene.{input => jfxi}

/**
 * Editable Label
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Label(val _text: String) extends RichJPane
with ViewableNode with HaveOperations with DragableNode[jp] with ZoomableNode[jp] with ScalableElement[jp]
with Data
with Transformable[Label] with MouseTransformable[jp]
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
      view ! ShowContextMenu(this)
      e.consume()
    }
  }

  textArea.textProperty().addListener((s: String) => updateSize(s))


  textArea.addEventFilter(jfxi.KeyEvent.KEY_PRESSED, (e: jfxi.KeyEvent) => e.getCode match {
    case jfxi.KeyCode.ENTER => e.consume()
    case jfxi.KeyCode.SPACE => e.consume()
    case _ => Unit
  })

  textArea.addEventFilter(jfxi.KeyEvent.KEY_RELEASED, (e: jfxi.KeyEvent) => e.getCode match {
    case jfxi.KeyCode.ENTER if e.isShiftDown =>
      insertEnter()
      e.consume()
    case jfxi.KeyCode.ENTER if !e.isShiftDown =>
      finishEditing(e)
      e.consume()
    case _ => Unit
  })

  def this() = this("")


  //
  // Data
  //

  override val dataType: String = "Text"

  override val storageFields = Map("ag:Text/text" -> (() => text))


  //
  // Transformation
  //

  override protected def createCloned(): Unit = {
    val clone = transformed(cloneParameters).original
    clone.swapSources(this)
    view ! ImportElement(clone)
  }

  override protected def copy: Label = {
    val label = new Label(text)
    label.node = node
    label.position = position.clone()
    label
  }

  //
  // Operations
  //

  def operations: Map[String, ActionEvent => Unit] = Map(
    "Edit" -> ((e: ActionEvent) => editable = true),
    "Delete" -> ((e: ActionEvent) => view ! DeleteElement(this))
  )

  def editable: Boolean = editMode


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

  private def finishEditing(e: jfxi.KeyEvent): Unit = {
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