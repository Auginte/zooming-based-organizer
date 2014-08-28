package com.auginte.desktop

import java.io.FileInputStream
import javafx.animation.KeyFrame
import javafx.scene.Node
import javafx.scene.layout.{Pane => jp}

import com.auginte.desktop.actors.{Container, DragableView, ZoomableView}
import com.auginte.desktop.events.{ImportElement, InsertElement, ShowContextMenu}
import com.auginte.desktop.nodes.{Label, MouseFocusable}
import com.auginte.desktop.rich.RichSPane
import com.auginte.desktop.zooming.{ZoomableCamera, ZoomableElement}
import com.auginte.distribution.data.{ImportedCamera, ImportedData, Camera, Data}
import com.auginte.distribution.repository.LocalStatic
import com.auginte.zooming.{AbsoluteDistance, Grid, Distance, IdToRealNode}

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
with Camera
with HaveOperations
with Storage {
  val repository = new LocalStatic
  val elements = () => d.getChildren flatMap { case d: Data => Some(d) case _ => None}
  val cameras = () => List(this)

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

  def remove(element: Node): Unit = content.remove(element)

  override def operations: Operations = Map(
    "Open" -> ((e: ActionEvent) => load()),
    "Save" -> ((e: ActionEvent) => save()),
    "Exit" -> ((e: ActionEvent) => HelloScalaFX.quit())
  )

  //
  // Load/Save
  //

  private def load(): Unit = {
    val path = "/home/aurelijus/Documents/DiNoSy/auginte/auginte-distribution/src/test/resources/com/auginte/distribution/repository/localStatic/simple.json"
    val input = new FileInputStream(path)
    val (newGrid, elements, cameras) = repository.load(input, elementCreator, cameraCreator)
    grid = newGrid
    val camera: AbsoluteDistance = if (cameras.nonEmpty) cameras(0) else defaultCamera
    node = camera._1
    position = camera._2
    for (e <- elements.flatten) {
      view ! ImportElement(e)
    }
  }

  private def defaultCamera: AbsoluteDistance = (grid.root, Distance(0, 0, 1))

  private val gridCreator = (grid: Grid) => new Grid {
    override def root = grid.root
  }

  private val elementCreator =
    (data: ImportedData, map: IdToRealNode) => data match {
      case ImportedData(id, typeName, x, y, scale, nodeId, customFields) => typeName match {
        case "ag:Text" if customFields.contains("text") =>
          val label = new Label() {
            text = customFields("text")
            position = Distance(x, y, scale)
            node = map(nodeId)
          }
          Some(label)
        case _ => None
      }
      case _ => None
    }

  private val cameraCreator =
    (camera: ImportedCamera, map: IdToRealNode) => (map(camera.nodeId), Distance(camera.x, camera.y, camera.scale))

  private def save(): Unit = {

  }

  //
  // Operations
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) {
      view ! ShowContextMenu(this)
    }
  }

  private def initContextMenu() = {
    val contextMenu = new ContextMenu()
    contextMenu.layoutX <== (width - contextMenu.width) / 2
    contextMenu.layoutY <== height - contextMenu.height
    content += contextMenu
    contextMenu.hide()
    contextMenu
  }
}