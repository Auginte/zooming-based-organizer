package com.auginte.desktop

import java.io.{FileOutputStream, File, FileInputStream}
import javafx.animation.KeyFrame
import javafx.concurrent.Task
import javafx.scene.Node
import javafx.scene.layout.{Pane => jp}

import com.auginte.desktop.actors.{Container, DragableView, ZoomableView}
import com.auginte.desktop.events.{ImportElement, InsertElement, ShowContextMenu}
import com.auginte.desktop.nodes.{Label, MouseFocusable}
import com.auginte.desktop.rich.RichSPane
import com.auginte.desktop.zooming.{ZoomableCamera, ZoomableElement}
import com.auginte.distribution.data.{ImportedCamera, ImportedData, Camera, Data}
import com.auginte.distribution.exceptions.{UnconnectedIds, UnsupportedElement, ImportException}
import com.auginte.distribution.repository.LocalStatic
import com.auginte.zooming.{AbsoluteDistance, Grid, Distance, IdToRealNode}

import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.Scene
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.util.Duration
import javafx.{scene => jfxs}

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
  val extensionsFilter = new ExtensionFilter("Static Text repository (*.json)", "*.json")
  val nonBlockingWindow = new Scene().getWindow
  val fileChooser = new FileChooser {
    title = "Choose repository file"
    extensionFilters add extensionsFilter
  }
  private var repositoryPath: Option[String] = None

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
    "Open" -> ((e: ActionEvent) => open()),
    "Save" -> ((e: ActionEvent) => if (repositoryPath.isDefined) save(repositoryPath.get) else saveAs(e)),
    "Save As" -> ((e: ActionEvent) => saveAs(e)),
    "Exit" -> ((e: ActionEvent) => HelloScalaFX.quit())
  )

  //
  // Load/Save
  //

  private def open(): Unit = {
    fileChooser.showOpenDialog(nonBlockingWindow) match {
      case f: File if f.canRead => load(f.getPath)
      case cancelled => println("Cancel")
    }
  }

  private def load(path: String): Unit = {
    def updateCamera(cameras: Seq[AbsoluteDistance]): Unit = {
      val camera: AbsoluteDistance = if (cameras.nonEmpty) cameras(0) else defaultCamera
      node = camera._1
      position = camera._2
    }
    def updateElements(elements: Seq[Option[jfxs.Node]]): Unit = {
      Platform.runLater {
        d.getChildren.clear()
        d.getChildren.add(0, contextMenu)
      }
      d.getChildren.clear()
      for (e <- elements.flatten) {
        view ! ImportElement(e)
      }
    }

    try {
      val input = new FileInputStream(path)
      val (newGrid, elements, cameras) = repository.load(input, elementCreator, cameraCreator)
      grid = newGrid
      updateCamera(cameras)
      updateElements(elements)
      repositoryPath = Some(path)
      validateZoomableElementsLater()
    } catch {
      case importException: ImportException => println(s"Import Exception: $importException")
      case e: Exception => println(s"Other Exception: $e")
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
          val label = new Label(customFields.getOrElse("text", "")) {
            position = Distance(x, y, scale)
            if (map.contains(nodeId)) {
              node = map(nodeId)
            } else {
              throw UnconnectedIds(id, nodeId)
            }
          }
          Some(label)
        case _ => None
      }
      case _ => None
    }

  private val cameraCreator =
    (camera: ImportedCamera, map: IdToRealNode) => (map(camera.nodeId), Distance(camera.x, camera.y, camera.scale))

  private def saveAs(e: ActionEvent, blocking: Boolean = false): Unit = {
    def withExtension(path: String) = if (path.endsWith(".json")) path else path + ".json"

    fileChooser.showSaveDialog(nonBlockingWindow) match {
      case f: File  => save(withExtension(f.getPath))
      case cancelled => Unit
    }
  }

  private def save(path: String): Unit = {
    try {
      val stream = new FileOutputStream(path)
      repository.save(stream, grid, elements, cameras)
      stream.close()
      repositoryPath = Some(path)
    } catch {
      case e: Exception => println(s"Saving failed: $e")
    }
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