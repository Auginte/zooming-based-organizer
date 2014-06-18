package com.auginte.desktop.rich

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}
import javafx.beans.property.StringProperty

/**
 * Delegating rich functionality to JavaFx TextArea
 * Needed for d initialisation before other traits (Linearization Algorithm for Reference Types)
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RichJTextArea extends jfxc.TextArea with RichNode[jfxc.TextArea] {
  protected[desktop] val d = this

  def prefWidth = getPrefWidth()

  def prefWidth_=(a: Double): Unit = setPrefWidth(a)

  def prefHeight = getPrefWidth()

  def prefHeight_=(a: Double): Unit = setPrefHeight(a)

  def text = textProperty

  def text_=(textProperty: StringProperty): Unit = text = textProperty

  def styleClass = getStyleClass
}