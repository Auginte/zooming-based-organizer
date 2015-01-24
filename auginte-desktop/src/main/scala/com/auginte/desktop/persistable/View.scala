package com.auginte.desktop.persistable

import javafx.scene.{layout => jfxl}

import com.auginte.common.Unexpected
import com.auginte.desktop.nodes.InitialSize
import com.auginte.desktop.rich.{RichSPane, RichJPane, RichNode}
import com.auginte.desktop.zooming.{ZoomableNode, ZoomableElement}
import com.auginte.distribution.orientdb._
import com.auginte.zooming.{Grid, GlobalCoordinates, Coordinates, Node}

import scalafx.Includes._

/**
 * Operations to convert between GUI and infinity zooming coordinates.
 *
 * Using camera (node + coordinates) and individual element (node + coordinate) position.
 *
 * @see [[com.auginte.zooming.Grid.absolute( )]]
 */
trait View
  extends GridWrapper
  with CameraWrapper
{

  private val scale = 2
  private val deep = 4
  private val parents = 2

  protected val boundary = 1E6
  protected val boundarySize = 1E5


  //
  // Update camera coordinate
  //

  def zoom(amount: Double, x: Double = 0, y: Double): Coordinates = state match {
    case PersistedGrid(grid, camera, _, _) =>
      camera.coordinates = grid.zoomCamera(camera.coordinates, amount, x, y)
      validateCameraNode()
      camera.coordinates
    case _ => Coordinates()
  }

  private def validateCameraNode(): Unit = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      updateIf[GlobalCoordinates](camera.globalCoordinates = _)(node != _._1)(grid.optimiseCamera(node, coordinates))
    case _ => Unit
  }

  def translate(x: Double, y: Double): Coordinates = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      assignReturnAnd[Coordinates](camera.coordinates = _) {
        grid.translateCamera(coordinates, -x, -y)
      } {
        if (visibleDifference(x, y)) validateCameraNode()
      }
    case _ => Coordinates()
  }

  @inline
  private def visibleDifference(x: Double, y: Double) = !approximately(x, 1) && !approximately(y, 1)

  @inline
  private def approximately(number: Double, epsilon: Double) = number - epsilon <= 0 && number + epsilon >= 0


  //
  // Update absolute coordinates
  //

  def translate(element: RepresentationWrapper, x: Double, y: Double): GlobalCoordinates = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      assignReturnAnd[GlobalCoordinates](element.storage.globalCoordinates = _) {
        grid.translateElement(element.storage.node, element.storage.coordinates, x, y, node, coordinates)
      } {
        element.storage.save()
      }
    case _ => element.storage.globalCoordinates
  }

  def scale(element: RepresentationWrapper, scaleDiff: Double): GlobalCoordinates = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      assignReturnAnd[GlobalCoordinates](element.storage.globalCoordinates = _){
        grid.scaleElement(element.storage.node, element.storage.coordinates, scaleDiff)
      } {
        element.storage.save()
      }
    case _ => element.storage.globalCoordinates
  }

  def fromCameraView(element: RepresentationWrapper, x: Double, y: Double): Unit = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      assignReturnAnd[GlobalCoordinates](element.storage.globalCoordinates = _) {
        grid.newElement(node, coordinates, x, y)
      } {
        element.storage.save()
      }
    case _ => Unexpected.state(s"Trying to update element position from camera, but not persisted: $this, $element")
  }


  //
  // Absolute to cached coordinates
  //

  protected def absoluteToCachedCoordinates(elements: Iterator[GuiRepresentation]): Unit = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      val visibleByCamera = filterByNodesAround(elements, node)
      visibleByCamera.foreach(absoluteToCached)
      val farAway = elements.filterNot(visibleByCamera.contains(_))
      farAway.foreach(_.setVisible(false))
    case _ => Unit
  }

  private def filterByNodesAround(elements: Iterator[GuiRepresentation], center: Node): Iterator[GuiRepresentation] = {
    //FIXME: Use orientDb
    elements
  }


  private def absoluteToCached(element: GuiRepresentation): Unit = state match {
    case PersistedGrid(grid, camera, node, coordinates) =>
      val representation = element.storage
      val absolute = grid.absolute(node, coordinates, representation.node, representation.coordinates)
      if (inVisibilityBoundaries(absolute)) {
        absoluteToCached(element, absolute)
        element.setVisible(true)
      } else {
        element.setVisible(false)
      }
    case _ => Unit
  }

  private def absoluteToCached(element: GuiRepresentation, absolute: Coordinates): Unit = {
    val (pivotX, pivotY) = elementsPivot(element)
    element.setScaleX(absolute.scale)
    element.setScaleY(absolute.scale)
    element.setLayoutX(absolute.x - pivotX)
    element.setLayoutY(absolute.y - pivotY)
  }

  private def elementsPivot(element: GuiRepresentation) = {
    val bound = element.getLayoutBounds
    val pivotX = if (bound.getWidth > 0) bound.getMinX + (bound.getWidth / 2) else InitialSize.width / 2
    val pivotY = if (bound.getHeight > 0) bound.getMinY + (bound.getHeight / 2) else InitialSize.height / 2
    (pivotX, pivotY)
  }

  protected def inVisibilityBoundaries(pos: Coordinates): Boolean = {
    pos.x.abs < boundary && pos.y.abs < boundary && pos.scale < boundarySize && pos.scale > 1 / boundarySize
  }


  //
  // Utilities
  //

  private def updateIf[A](update: A => Unit)(needUpdate: A => Boolean)(computation: => A): Unit = {
    val computed = computation
    if (needUpdate(computed)) update(computed)
  }

  private def assignReturnAnd[A](update: A => Unit)(compute: => A)(cache: => Unit): A = {
    val result = compute
    update(result)
    cache
    result
  }

  //
  // Solving not persisted elements
  //

  private def state: StorageState =
    if (this.grid.isDefined && this.camera.isDefined) {
      PersistedGrid(this.grid.get, this.camera.get, this.camera.get.node, this.camera.get.coordinates)
    } else NotPersisted


  private sealed trait StorageState

  private case class PersistedGrid(grid: Grid, camera: Camera, cameraNode: Node, cameraPos: Coordinates)
    extends StorageState

  private object NotPersisted extends StorageState
}