package com.auginte.desktop

import javafx.animation.KeyFrame
import javafx.scene.Node
import javafx.scene.layout.{Pane => jp}

import com.auginte.desktop.actors.{Container, DragableView, ZoomableView}
import com.auginte.desktop.events.ShowContextMenu
import com.auginte.desktop.nodes.MouseFocusable
import com.auginte.desktop.rich.RichSPane
import com.auginte.desktop.zooming.ZoomableCamera
import com.auginte.distribution.data.Data
import com.auginte.distribution.repository.LocalStatic

import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.event.ActionEvent
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.util.Duration

/**
 * JavaFX panel with Infinity zooming layout.
 * Can share content with over Views.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends RichSPane
with Container[jp] with DragableView[jp] with ZoomableView[jp]
with ZoomableCamera[jp] with MouseFocusable[jp]
with HaveOperations {
  val contextMenu = initContextMenu()

  private lazy val repository = {
    new LocalStatic(grid, () => d.getChildren flatMap { case d: Data => Some(d) case _ => None})
  }

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

  def remove(element: Node): Unit = content.remove(element)

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    contextMenu.layoutX <== (width - contextMenu.width) / 2
    contextMenu.layoutY <== height - contextMenu.height
    content += contextMenu
    contextMenu.hide()
    contextMenu
  }

  //
  // Operations
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
    }
  }

  override def operations: Operations = Map(
    "Open" -> ((e: ActionEvent) => repository.load()),
    "Save" -> ((e: ActionEvent) => repository.save()),
    "Exit" -> ((e: ActionEvent) => HelloScalaFX.quit())
  )
}