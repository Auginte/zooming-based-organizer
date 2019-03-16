package com.auginte.desktop

import java.awt.Desktop
import java.io.File
import java.net.URI

import com.auginte.common.SoftwareVersion
import com.auginte.desktop.actors.{Container, DragableView, ZoomableView}
import com.auginte.desktop.events.{EditElement, InsertElement}
import com.auginte.desktop.nodes.{Label, MouseFocusable}
import com.auginte.desktop.operations._
import com.auginte.desktop.rich.RichSPane
import com.auginte.desktop.storage.{Loading, Saving, WithFileChooser}
import com.auginte.desktop.zooming.ZoomableCamera
import com.auginte.distribution.data.Camera
import com.auginte.distribution.exceptions.ImportException
import com.auginte.distribution.repository.LocalStatic
import javafx.animation.KeyFrame
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.{Pane => jp}

import com.auginte.desktop.utilities.Logger

import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.event.ActionEvent
import scalafx.util.Duration
import scala.collection.immutable.HashMap

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
with Saving[jp] with Loading[jp] with WithFileChooser {
  val repository = new LocalStatic
  private var repositoryPath: Option[String] = None

  override def content: ObservableList[Node] = children

  //
  // Infinity zooming and refresh
  //

  private val grid2absoluteCron = new Timeline {
    cycleCount = Timeline.INDEFINITE
    keyFrames = Seq(
      jfxKeyFrame2sfx(new KeyFrame(
        Duration(10),
        (e: ActionEvent) => absoluteToCachedCoordinates()
      ))
    )
  }
  grid2absoluteCron.play()

  def remove(element: Node): Unit = content.remove(element)


  //
  // Operations and context menu
  //

  override def operations: Operations = HashMap(
    "New" -> ((e: ActionEvent) => clearElements()),
    "Add text" -> ((e: ActionEvent) => addElement(new Label(""))),
    "Open" -> ((e: ActionEvent) => open()),
    "Save" -> ((e: ActionEvent) => if (repositoryPath.isDefined) save(repositoryPath.get) else saveAs(e)),
    "Save As" -> ((e: ActionEvent) => saveAs(e)),
    "Feedback" -> ((e: ActionEvent) => feedback()),
    "Exit" -> ((e: ActionEvent) => Auginte.quit())
  )

  override protected def layoutContextMenu(menu: ContextMenu): Unit = {
    menu.layoutX <== (width - menu.width) / 2
    menu.layoutY <== height - menu.height
  }

  //
  // Load/Save
  //


  private def open(): Unit = {
    fileChooser.showOpenDialog(nonBlockingWindow) match {
      case f: File if f.canRead => try {
        load(f.getPath)
      } catch {
        case importException: ImportException => System.err.println(s"Import Exception: $importException")
        case e: Exception => System.err.println(s"Other file load Exception: $e")
      }
      case cancelled => Unit
    }
  }

  override def load(path: String): Unit = Logger.loadingFile {
    super.load(path)
    repositoryPath = Some(path)
    validateZoomableElementsLater()
  }

  private def saveAs(e: ActionEvent, blocking: Boolean = false): Unit = {
    fileChooser.showSaveDialog(nonBlockingWindow) match {
      case f: File => save(withExtension(f.getPath))
      case cancelled => Unit
    }
  }

  private def feedback(): Unit = {
    new Thread {
      override def run() {
        Desktop.getDesktop.browse(new URI(s"http://auginte.com/en/contact"))
      }
    }.start()
  }

  private def addElement[T <: Node](element: T): Unit =
  {
    view ! InsertElement(element, d.getWidth / 2, d.getHeight / 2)
    element match {
      case e: EditableNode => view ! EditElement(e, mode=true)
      case _ => Unit
    }
  }

  override protected def save(path: String): Unit = Logger.save {
    try {
      super.save(path)
      repositoryPath = Some(path)
    } catch {
      case e: Exception => println(s"Saving failed: $e")
    }
  }
}