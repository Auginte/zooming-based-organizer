package com.auginte.desktop.zooming

import javafx.scene.{layout => jfxl}

import com.auginte.desktop.events.ElementUpdated
import com.auginte.desktop.nodes.InitialSize
import com.auginte.desktop.rich.RichNode
import com.auginte.zooming.{AbsoluteDistance, Distance, Node}

import scalafx.Includes._
import scalafx.event.ActionEvent

/**
 * Zooming related functionality for containers.
 *
 * @see [[com.auginte.zooming.Grid.absolute( )]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera[D <: jfxl.Pane] extends RichNode[D]
with ZoomableElement {

  private val epsilonTranslate = 1
  private val scale = 2
  private val deep = 4
  private val parents = 2
  private val boundary = 1E6
  private val boundarySize = 1E5

  private var visibleElements: List[ZoomableNode[D]] = List()
  @volatile private var revalidateZoomable: Boolean = false

  /**
   * Updates scaling.
   *
   * @param amount amount of scaling
   * @param x GUI zoom center
   * @param y GUI zoom center
   * @return updated coordinates
   */
  def zoom(amount: Double, x: Double = 0, y: Double): Distance = {
    position = grid.zoomCamera(position, amount, x, y)
    validateCameraNode()
    position
  }

  private def validateCameraNode(): Unit = {
    val optimized = grid.validateCamera(node, position)
    if (node != optimized._1) {
      node = optimized._1
      position = optimized._2
    }
  }

  def validateZoomableElementsLater(): Unit = {
    revalidateZoomable = true
  }

  /**
   * Updates coordinates. Converts GUI translation to camera's absolute coordinates.
   *
   * @param x GUI translation
   * @param y GUI translation
   * @return updated absolute coordinates
   */
  override def translate(x: Double, y: Double): Distance = {
    position = grid.translateCamera(position, x, y)
    if (!equalDouble(x, 0, epsilonTranslate) && !equalDouble(y, 0, epsilonTranslate)) {
      validateCameraNode()
    }
    position
  }

  @inline
  private def equalDouble(a: Double, b: Double, epsilon: Double) = a - epsilon <= b && a + epsilonTranslate >= b

  /**
   * Updates coordinates. Converts GUI element's translation to absolute coordinates,
   * as calculation involves camera's coordinates.
   *
   * @param element child element, which coordinates should be updated.
   * @param x GUI translation
   * @param y GUI translation
   * @return updated element's absolute coordinates
   */
  def translate(element: ZoomableElement, x: Double, y: Double): AbsoluteDistance = {
    val (newNode, newTransformation) = grid.translateElement(element.node, element.position, x, y, node, position)
    element.position = newTransformation
    element.node = newNode
    (newNode, newTransformation)
  }

  /**
   * Updates coordinates. Converts GUI element's scale to absolute coordinates,
   * as calculation involves camera's coordinates.
   *
   * @param element child element, which coordinates should be updated.
   * @param scaleDiff amount
   * @return updated element's absolute coordinates
   */
  def scale(element: ZoomableElement, scaleDiff: Double): AbsoluteDistance = {
    val (newNode, newTransformation) = grid.scaleElement(element.node, element.position, scaleDiff)
    element.node = newNode
    element.position = newTransformation
    (newNode, newTransformation)
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
    val optimised = grid.newElement(node, position, x, y)
    element.node = optimised._1
    element.position = optimised._2
  }

  /**
   * For every child, converts infinity zooming coordinates to GUI ones
   */
  protected def absoluteToCachedCoordinates(): Unit = if (revalidateZoomable) {
    def isInBoundaries(pos: Distance): Boolean = {
      pos.x.abs < boundary && pos.y.abs < boundary && pos.scale < boundarySize && pos.scale > 1 / boundarySize
    }

    def getParent(from: Node, deep: Int): Node = if (deep <= 0) from
    else getParent(grid.getNode(from, 0, 0, 100), deep - 1)

    def javaFx2zoomable: (ZoomableNode[D]) => Node = _.node

    def getZoomable: List[ZoomableNode[D]] = {
      var list: List[ZoomableNode[D]] = List()
      for (element <- d.getChildren) element match {
        case e: ZoomableNode[D] => list = e :: list
        case _ => Unit
      }
      list
    }

    def haveInitialSize(e: ZoomableNode[D]) = {
      val bound = e.d.getLayoutBounds
      bound.getWidth > 0 && bound.getHeight > 0
    }

    val zoomable = getZoomable
    try {
      val parent = getParent(node, parents)
      visibleElements = grid.filter(zoomable, parent, scale, deep, javaFx2zoomable).toList
    } catch {
      case e: Exception => println(e)
    }
    val hiddenElements = zoomable diff visibleElements

    revalidateZoomable = false
    for (e <- visibleElements) {
      val absolute = grid.absolute(node, position, e.node, e.position)
      val wihInitialSize = haveInitialSize(e)
      if (isInBoundaries(absolute) && wihInitialSize) {
        try {
          val bound = e.d.getLayoutBounds
          val pivotX = if (bound.getWidth > 0) bound.getMinX + (bound.getWidth / 2) else InitialSize.width / 2
          val pivotY = if (bound.getHeight > 0) bound.getMinY + (bound.getHeight / 2) else InitialSize.height / 2
          e.d.setScaleX(absolute.scale)
          e.d.setScaleY(absolute.scale)
          e.d.setScaleZ(absolute.scale)
          e.d.setLayoutX(absolute.x - pivotX)
          e.d.setLayoutY(absolute.y - pivotY)
          e.d.setVisible(true)
        } catch {
          case e: Exception => println(e)
        }
      } else {
        e.d.setVisible(false)
      }
      if (!wihInitialSize) {
        revalidateZoomable = true
      }
    }
    for (e <- hiddenElements) e.d.setVisible(false)
  }
}