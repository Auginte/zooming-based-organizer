package com.auginte.desktop.rich

import javafx.scene.{layout => jfxl}
import javafx.scene.{input => jfxi}
import scalafx.scene.{input => sfxi}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.beans.property.DoubleProperty
import javafx.collections.ObservableList
import scala.language.implicitConversions

import java.beans.EventHandler
import javafx.{event => jfxe}

/**
 * Delegating Rich functionality to JavaFx Pane
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RichJPane extends jfxl.Pane with RichNode[jfxl.Pane] {
  override val d = this

  //
  // Listeners
  //

  implicit def closure2StringChangeListener(f: (String) => Any): ChangeListener[String] =
    closure2ChangeListener[String](f)

  implicit def closure2KeyListener(f: (sfxi.KeyEvent) => Any): jfxe.EventHandler[jfxi.KeyEvent] =
    closure2EventHandler[jfxi.KeyEvent, sfxi.KeyEvent](f, new sfxi.KeyEvent(_))

  private def closure2ChangeListener[A](f: (A) => Any): ChangeListener[A] = new ChangeListener[A] {
    override def changed(p1: ObservableValue[_ <: A], from: A, to: A): Unit = f(to)
  }

  private def closure2EventHandler[J <: jfxe.Event, S](f: (S) => Any, adapter: (J) => S): jfxe.EventHandler[J] =
    new jfxe.EventHandler[J] {
    def handle(event: J) = {
      f(adapter(event))
    }
  }


  //
  // Geometry
  //

  def prefWidth: DoubleProperty = prefWidthProperty()

  def prefWidth_=(v: Double) = prefWidth.setValue(v)


  def prefHeight: DoubleProperty = prefHeightProperty()

  def prefHeight_=(v: Double) = prefHeight.setValue(v)

  //
  // Style
  //

  def styleClass: ObservableList[String] = getStyleClass

  def stylesheets: ObservableList[String] = getStylesheets
}
