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


  lazy val scrolled = new Event[sfxi.ScrollEvent, jfxi.ScrollEvent](d.onScrollProperty(), new sfxi.ScrollEvent(_))

  def scrolled_=(f: sfxi.ScrollEvent => Any): Unit = scrolled.replace(f)



  lazy val mouseEntered = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseEnteredProperty(), new sfxi.MouseEvent(_))

  def mouseEntered_=(f: sfxi.MouseEvent => Any): Unit = mouseEntered.replace(f)


  lazy val mouseExited = new Event[sfxi.MouseEvent, jfxi.MouseEvent](d.onMouseExitedProperty(), new sfxi.MouseEvent(_))

  def mouseExited_=(f: sfxi.MouseEvent => Any): Unit = mouseExited.replace(f)



  lazy val keyPressed = new Event[sfxi.KeyEvent, jfxi.KeyEvent](d.onKeyPressedProperty(), new sfxi.KeyEvent(_))

  def keyPressed_=(f: sfxi.KeyEvent => Any): Unit = keyPressed.replace(f)


  lazy val keyReleased = new Event[sfxi.KeyEvent, jfxi.KeyEvent](d.onKeyReleasedProperty(), new sfxi.KeyEvent(_))

  def keyReleased_=(f: sfxi.KeyEvent => Any): Unit = keyReleased.replace(f)


  lazy val keyTyped = new Event[sfxi.KeyEvent, jfxi.KeyEvent](d.onKeyTypedProperty(), new sfxi.KeyEvent(_))

  def keyTyped_=(f: sfxi.KeyEvent => Any): Unit = keyTyped.replace(f)
}