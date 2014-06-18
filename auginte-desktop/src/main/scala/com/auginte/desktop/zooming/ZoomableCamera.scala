package com.auginte.desktop.zooming

import javafx.scene.control.TableView
import javafx.scene.{Node => jn, layout => jfxl}
import javafx.{scene => jfxs}

import com.auginte.desktop.nodes.MapRow
import com.auginte.desktop.rich.RichNode
import com.auginte.zooming.{Distance, Node}

import scalafx.Includes._

/**
 * Zooming related functionality for containers.
 *
 * @see [[com.auginte.zooming.Grid.absolute( )]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera[D <: jfxl.Pane] extends RichNode[D]
with ZoomableElement {

  val maxVisibleSize = 1E5
  val minVisibleSize = 0.5
  val epsilonTranslate = 1

  //FIXME: debug
  var mapTable: Option[TableView[MapRow]] = None

  //FIXME:
  debug(
    """
      |val (c1, grid) = rootGridPair()
      |var g: Distance = Distance()
      |var c = c1
      |var t = Distance()
      |var pair = (c1, Distance())
      |var optimised = (c1, Distance())
    """.stripMargin)

  /**
   * For every child, converts infinity zooming coordinates to GUI ones
   */
  protected def absoluteToCachedCoordinates(): Unit = for (element <- d.getChildren) element match {
    case e: ZoomableNode[D] => {
      val absolute = grid.absolute(node, transformation, e.node, e.transformation)
      e.setTranslateX(absolute.x)
      e.setTranslateY(absolute.y)
      e.setScaleX(absolute.scale)
      e.setScaleY(absolute.scale)
      e.setScaleZ(absolute.scale)
      val minSize = Math.min(e.getLayoutBounds.getWidth, e.getLayoutBounds.getHeight)
      val maxSize = Math.max(e.getLayoutBounds.getWidth, e.getLayoutBounds.getHeight)
      e.setVisible(minSize > minVisibleSize && maxSize < maxVisibleSize)
    }
    case _ => Unit
  }

  protected def debugHierarchy(): Unit = {
//    var map: Grid#GridMap = Map()
//    for (element <- d.getChildren) element match {
//      case e: ZoomableNode[jn] => {
//        val elementNode: Node = grid.getNode(node, e.t.x + t.x, e.t.y + t.y, e.t.scale * t.scale)
//        map = map ++ Map(e.d -> elementNode)
//        map.values
//      }
//      case _ => Unit
//    }
//    if (mapTable.isDefined) {
//      mapTable.get.setItems(MapRow.fromMap(map))
//    }
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
    debug(s"t = grid.zoomCamera($transformation, $amount, $x, $y)")
    transformation = grid.zoomCamera(transformation, amount, x, y)
    validateCameraNode()
    transformation
  }

  private def validateCameraNode(): Unit = {
    val optimized = grid.validateCamera(node, transformation)
    if (node != optimized._1) {
      debug(s"optimised = grid.validateCamera(c, t)")
      debug(s"c = optimised._1")
      debug(s"t = optimised._2")
      if (DebugZoomable.on) debugAbsoluteToCachedCoordinates(node, transformation, optimized._1, optimized._2)
      node = optimized._1
      transformation = optimized._2
    }
  }

  protected def debugAbsoluteToCachedCoordinates(camera1: Node, absolute1: Distance, camera2: Node, absolute2: Distance): Unit = synchronized {
    for (element <- d.getChildren) element match {
      case e: ZoomableNode[D] => {
        val g1 = grid.absolute(camera1, absolute1, e.node, e.transformation)
        val g2 = grid.absolute(camera2, absolute2, e.node, e.transformation)
        val elementVar = "n" + e.debugId
        val absoluteVar = "a" + e.debugId
        debug(s"// $camera1 $absolute1 --> $camera2 $absolute2")
        debug(s"assertDistance($g1, grid.absolute(c, t, $elementVar, $absoluteVar), precision)")
        if (!compareDistance(g1, g2)) {
          debug(s"// $g1 -> $g2")
          debug(s"val g1 = grid.absolute(c, t, $elementVar, $absoluteVar)")
          debug(s"val g2 = grid.absolute(c, t, $elementVar, $absoluteVar)")
          debug(s"assertDistance(g1, g2, precision)")
          debug("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        }
      }
      case _ => Unit
    }
  }

  private def compareDistance(expected: Distance, actual: Distance, t: Double = 1E-7) = {
    expected.x > actual.x - t && expected.x < actual.x + t &&
      expected.y > actual.y - t && expected.y < actual.y + t &&
      expected.scale > actual.scale - t && expected.scale < actual.scale + t
  }

  /**
   * Updates coordinates. Converts GUI translation to camera's absolute coordinates.
   *
   * @param x GUI translation
   * @param y GUI translation
   * @return updated absolute coordinates
   */
  override def translate(x: Double, y: Double): Distance = {
    transformation = grid.translateCamera(transformation, x, y)
    if (!equalDouble(x, 0, epsilonTranslate) && !equalDouble(y, 0, epsilonTranslate)) {
      debug(s"t = grid.translateCamera(t, $x, $y)")
      validateCameraNode
    }
    transformation
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
  def translate(element: ZoomableElement, x: Double, y: Double): Distance = {
    element.transformation = grid.translateElement(element.transformation, x, y, transformation)
    val elementVar = "n" + element.debugId
    debug(s"$elementVar = grid.translateElement(${elementVar}.transformation, $x, $y, t)")
    element.transformation
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
    val optimised = grid.newElement(node, transformation, x, y)
    element.node = optimised._1
    val elementVar = "n" + element.debugId
    val absoluteVar = "a" + element.debugId
    debug(s"pair = grid.newElement(c, t, $x, $y)")
    debug(s"val ($elementVar, $absoluteVar) = pair")
    element.transformation = optimised._2
    element.transformation
  }
}