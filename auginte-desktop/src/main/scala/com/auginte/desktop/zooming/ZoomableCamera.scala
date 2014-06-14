package com.auginte.desktop.zooming

import javafx.scene.{layout => jfxl}
import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}
import javafx.scene.{Node => jn}
import scala.collection.JavaConversions._
import com.auginte.zooming.{Node, Distance}
import javafx.scene.control.TableView
import com.auginte.desktop.nodes.MapRow

/**
 * Zooming related functionality for containers.
 *
 * @see [[com.auginte.zooming.Grid.absolute()]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera[D <: jfxl.Pane] extends RichNode[D]
with ZoomableElement {

  //FIXME: debug
  var mapTable: Option[TableView[MapRow]] = None

  /**
   * For every child, converts infinity zooming coordinates to GUI ones
   */
  protected def absoluteToCachedCoordinates(): Unit = for (element <- d.getChildren) element match {
    case e: ZoomableNode[D]  => {
      val absolute = grid.absolute(node, transformation, e.node, e.transformation)
      e.setTranslateX(absolute.x)
      e.setTranslateY(absolute.y)
      e.setScaleX(absolute.scale)
      e.setScaleY(absolute.scale)
      e.setScaleZ(absolute.scale)
    }
    case _ => Unit
  }

  protected def debugHierarchy(): Unit = {
    var map:Grid#GridMap = Map()
    for (element <- d.getChildren) element match {
      case e: ZoomableNode[jn] => {
        val elementNode: Node = grid.getNode(node, e.t.x + t.x, e.t.y + t.y, e.t.scale * t.scale)
        map = map ++ Map(e.d -> elementNode)
        map.values
      }
      case _ => Unit
    }
    if (mapTable.isDefined) {
      mapTable.get.setItems(MapRow.fromMap(map))
    }
  }

  /**
   * Updates scaling.
   *
   * @param amount amount of scaling
   * @param x GUI zoom center
   * @param y GUI zoom center
   * @return updated coordinates
   */
  def zoom(amount: Double, x: Double = 0, y: Double): Distance = {
    val newScale = t.scale * amount
    val zoomCenterBefore = Distance(x / t.scale, y / t.scale, 1)
    val zoomCenterAfter = Distance(x / newScale, y / newScale, 1)
    transformation = (transformation - zoomCenterBefore + zoomCenterAfter).zoomed(amount)
    validateCameraNode()
    transformation
  }

  private def validateCameraNode(): Unit = {
    val oldNode = node
    val newNode = grid.getCameraNode(oldNode, t.x, t.y, t.scale)
    if (oldNode != newNode) {
      debugGraphical
      val newAbsolute  = grid.absoluteCamera(oldNode, newNode, transformation)
      println(s"val (camera, newCamera, translation) = ($oldNode, $newNode, $transformation)")
      println(s"val (newTranslation) = ($newAbsolute)")
      transformation = newAbsolute
      node = newNode
      debugGraphical
      println("___________________________________")
    }
    //TODO: camera absolute position should not extend grid cell boundaries.
  }

  def debugGraphical(): Unit = for (element <- d.getChildren) element match {
    case e: ZoomableNode[D]  => {
      val absolute = grid.absolute(node, transformation, e.node, e.transformation)
      println(s"val(n, a, g) = (${e.node}, ${e.transformation}, $absolute)")
      e.setTranslateX(absolute.x)
      e.setTranslateY(absolute.y)
      e.setScaleX(absolute.scale)
      e.setScaleY(absolute.scale)
      e.setScaleZ(absolute.scale)
    }
    case _ => Unit
  }

  /**
   * Updates coordinates. Converts GUI translation to camera's absolute coordinates.
   *
   * @param x GUI translation
   * @param y GUI translation
   * @return updated absolute coordinates
   */
  override def translate(x: Double, y: Double): Distance = {
    transformation = t.translated(x / t.scale, y / t.scale)
    validateCameraNode
    transformation
  }

  /**
   * Updates coordinates. Converts GUI element's translation to absolute coordinates,
   * as calculation involves camera's coordinates.
   *
   * @param element child element, which coordinates should be updated.
   * @param x GUI translation
   * @param y GUI translation
   * @return updated element's absolute coordinates
   */
  def translate(element: ZoomableElement, x: Double, y: Double): Distance = {
    val e = element
    e.transformation = e.t.translated(x / t.scale, y / t.scale)
    e.transformation
  }

  /**
   * Initiates absolute transformation from GUI coordinates.
   *
   * Also sets element's initial node.
   *
   * @param element element, that will be added to View
   * @param x GUI position
   * @param y GUI position
   * @return updated element's absolute coordinates
   */
  def initializeInfinityZooming(element: ZoomableElement, x: Double, y: Double): Unit = {
    val absolute = grid.absoluteNew(transformation, x, y)
    val elementNode = grid.getNode(node, absolute)
    val optimised = grid.absoluteNew(node, elementNode, absolute)
    element.node = elementNode
    element.transformation = optimised
    element.transformation
  }


}