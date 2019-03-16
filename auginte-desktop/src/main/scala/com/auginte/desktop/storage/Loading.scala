package com.auginte.desktop.storage

import java.io.{InputStream, FileInputStream}
import javafx.scene.{layout => jfxl}
import javafx.{scene => jfxs}

import com.auginte.desktop.events.ImportElement
import com.auginte.desktop.nodes.Label
import com.auginte.desktop.zooming.{UsingGrid, ZoomableElement}
import com.auginte.distribution.data.{Camera, ImportedCamera, ImportedData}
import com.auginte.distribution.exceptions.UnconnectedIds
import com.auginte.transforamtion.Relation
import com.auginte.zooming._

import scalafx.application.Platform

/**
 * Functionality for saving new project.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Loading[D <: jfxl.Pane] extends UsingRepository
with Clearing[D]
with Camera with ZoomableElement {
  private def defaultCamera: AbsoluteDistance = (grid.root, Distance(0, 0, 1))

  private val elementCreator =
    (data: ImportedData, map: IdToRealNode) => data match {
      case ImportedData(id, typeName, x, y, scale, nodeId, customFields, sourcePlaceholder) => typeName match {
        case "ag:Text" if customFields.contains("text") && map.contains(nodeId) =>
          val label = new Label(customFields.getOrElse("text", "")) {
            position = Distance(x, y, scale)
            node = map(nodeId)
            sources = sourcePlaceholder
          }
          Some(label)
        case "ag:Text" if !map.contains(nodeId) =>
          System.out.println("Ignoring unconnected id: ", UnconnectedIds(id, nodeId))
          None
        case _ => None
      }
      case _ => None
    }

  private val cameraCreator =
    (camera: ImportedCamera, map: IdToRealNode) => (map(camera.nodeId), Distance(camera.x, camera.y, camera.scale))

  /**
   * Loads elements and grid from file.
   *
   * Old elements are removed.
   *
   * @param path path to .json repository
   * @throws ImportException when file foramt is not valid
   */
  protected def load(path: String): Unit = {
    val input = new FileInputStream(path)
    loadFromStream(input)
  }

  protected[desktop] def loadFromStream(input: InputStream): Unit = {
    def updateCamera(cameras: Seq[AbsoluteDistance]): Unit = {
      val camera: AbsoluteDistance = if (cameras.nonEmpty) cameras(0) else defaultCamera
      node = camera._1
      position = camera._2
    }

    def updateElements(elements: Seq[Option[jfxs.Node]]): Unit = {
      clearElements()
      elements.flatten.foreach(view ! ImportElement(_))
    }

    val (newGrid, elements, cameras) = repository.load(input, elementCreator, cameraCreator)
    grid = newGrid
    updateCamera(cameras)
    updateElements(elements)
  }
}
