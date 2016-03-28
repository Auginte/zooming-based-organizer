package com.auginte.desktop

import java.awt.Desktop
import java.io.{InputStream, File}
import java.net.URI
import javafx.animation.KeyFrame
import javafx.scene.Node
import javafx.scene.layout.{Pane => jp}

import com.auginte.common.SoftwareVersion
import com.auginte.desktop.actors.{Container, DragableView, ZoomableView}
import com.auginte.desktop.events.{EditElement, InsertElement}
import com.auginte.desktop.nodes.{Image, Label, MouseFocusable}
import com.auginte.desktop.operations._
import com.auginte.desktop.rich.RichSPane
import com.auginte.desktop.storage.{Loading, Saving, WithFileChooser}
import com.auginte.desktop.zooming.ZoomableCamera
import com.auginte.distribution.data.Camera
import com.auginte.distribution.exceptions.ImportException
import com.auginte.distribution.repository.LocalStatic

import scala.collection.immutable.{HashMap, SortedMap}
import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.event.ActionEvent
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
with Saving[jp] with Loading[jp] with WithFileChooser {
  val repository = new LocalStatic
  private var repositoryPath: Option[String] = None


  //
  // Infinity zooming and refresh
  //

  private val grid2absoluteCron = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      jfxKeyFrame2sfx(new KeyFrame(
        Duration(10),
        (e: ActionEvent) => absoluteToCachedCoordinates()
      ))
    )
  }
  grid2absoluteCron.play()

  def remove(element: Node): Unit = children.remove(element)


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
      case f: File if f.canRead => load(f.getPath)
      case cancelled => Unit
    }
  }

  override protected def load(path: String): Unit = {
    try {
      super.load(path)
      repositoryPath = Some(path)
      validateZoomableElementsLater()
    } catch {
      case importException: ImportException => println(s"Import Exception: $importException")
      case e: Exception => println(s"Other Exception: $e")
    }
  }

  private def saveAs(e: ActionEvent, blocking: Boolean = false): Unit = {
    fileChooser.showSaveDialog(nonBlockingWindow) match {
      case f: File => save(withExtension(f.getPath))
      case cancelled => Unit
    }
  }

  private def feedback(): Unit = {
    try {
      val version = SoftwareVersion.toString
      Desktop.getDesktop.mail(new URI(s"mailto:aurelijus@auginte.com?subject=Feedback%20$version"))
    } catch {
      case e: Exception => Desktop.getDesktop.browse(new URI(s"http://auginte.com/en/contact"))
    }
  }

  private def addElement[T <: Node](element: T): Unit =
  {
    view ! InsertElement(element, d.getWidth / 2, d.getHeight / 2)
    element match {
      case e: EditableNode => view ! EditElement(e, mode=true)
      case _ => Unit
    }
  }

  override protected def save(path: String): Unit = {
    try {
      super.save(path)
      repositoryPath = Some(path)
    } catch {
      case e: Exception => println(s"Saving failed: $e")
    }
  }
}