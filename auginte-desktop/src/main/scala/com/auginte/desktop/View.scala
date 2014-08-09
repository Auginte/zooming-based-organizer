package com.auginte.desktop

import javafx.collections.ObservableList

import com.auginte.desktop.events.ShowContextMenu
import com.auginte.desktop.nodes.MouseFocusable
import scala.compat.Platform
import scalafx.Includes._
import com.auginte.desktop.rich.RichSPane
import javafx.scene.layout.{Pane => jp}
import javafx.scene.Node
import com.auginte.desktop.actors.{ZoomableView, DragableView, Container}
import com.auginte.desktop.zooming.ZoomableCamera
import scalafx.animation.Timeline
import javafx.animation.KeyFrame
import scalafx.application.Platform
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.util.Duration
import scalafx.event.ActionEvent

/**
 * JavaFX panel with Infinity zooming layout.
 * Can share content with over Views.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends RichSPane
with Container[jp] with DragableView[jp] with ZoomableView[jp]
with ZoomableCamera[jp] with MouseFocusable[jp]
with HaveOperations
{

  val contextMenu = initContextMenu()

  private val grid2absoluteCron = new Timeline {
    cycleCount = Timeline.INDEFINITE
    keyFrames = Seq(
      jfxKeyFrame2sfx(new KeyFrame(
        Duration(10),
        (e: ActionEvent) => absoluteToCachedCoordinates(e)
      ))
    )
  }
  grid2absoluteCron.play()

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    contextMenu.layoutX <== (width - contextMenu.width) / 2
    contextMenu.layoutY <== height - contextMenu.height
    content += contextMenu
    contextMenu.hide()
    contextMenu
  }

  def remove(element: Node): Unit = content.remove(element)


  //
  // Operations
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
    }
  }

  override def operations: Operations = Map(
    "Open" -> ((e: ActionEvent) => println("Open")),
    "Save" -> ((e: ActionEvent) => println("Save")),
    "Exit" -> ((e: ActionEvent) => HelloScalaFX.quit())
  )
}