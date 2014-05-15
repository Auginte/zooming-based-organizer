package com.auginte.desktop.rich

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}
import scalafx.{scene => sfxs}

/**
 * Mouse and key events.
 *
 * Using mouse... instead of onMouse... names, so ScalaFx and JavaFx code could be used together
 *
 * @see [[javafx.scene.input.MouseEvent]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait RichNodeGeneralEvents[D <: jfxs.Node] extends RichNodeDelegating[D] {

  lazy val mouseClicked = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseClickedProperty(), new sfxi.MouseEvent(_))

  def mouseClicked_=(f: sfxi.MouseEvent => Any): Unit = mouseClicked.replace(f)


  lazy val mouseMoved = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseMovedProperty(), new sfxi.MouseEvent(_))

  def mouseMoved_=(f: sfxi.MouseEvent => Any): Unit = mouseClicked.replace(f)


  lazy val mousePressed = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMousePressedProperty(), new sfxi.MouseEvent(_))

  def mousePressed_=(f: sfxi.MouseEvent => Any): Unit = mouseClicked.replace(f)


  lazy val mouseReleased = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseReleasedProperty(), new sfxi.MouseEvent(_))

  def mouseReleased_=(f: sfxi.MouseEvent => Any): Unit = mouseClicked.replace(f)



  lazy val mouseDragged = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseDraggedProperty(), new sfxi.MouseEvent(_))

  def mouseDragged_=(f: sfxi.MouseEvent => Any): Unit = mouseDragged.replace(f)
}