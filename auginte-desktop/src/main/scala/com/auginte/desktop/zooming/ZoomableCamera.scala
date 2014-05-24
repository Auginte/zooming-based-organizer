package com.auginte.desktop.zooming

import javafx.scene.{layout => jfxl}
import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}
import scala.collection.JavaConversions._
import com.auginte.zooming.Distance

/**
 * Zooming related functionality for containers.
 *
 * Transformations:
 * {{{
 *   G_p = (V_p + E_p) * V_s
 *   // G_p - Graphical user interface (GUI, e.g. JavaFx) position
 *   // V_p - View's (camera's) absolute position
 *   // E_p - Element's own absolute position
 *   // V_s - View's (camera's) scale (inverse of zooming)
 * }}}
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera[D <: jfxl.Pane] extends RichNode[D] with ZoomableElement {

  /**
   * For every child, converts infinity zooming coordinates to GUI ones
   */
  protected def absoluteToCachedCoordinates(): Unit = {
    for (element <- d.getChildren) element match {
      case e: ZoomableNode[D] => {
        val t = transformation
        e.setTranslateX((t.x + e.t.x) * t.scale)
        e.setTranslateY((t.y + e.t.y) * t.scale)
        e.setScaleX(e.t.scale * t.scale)
        e.setScaleY(e.t.scale * t.scale)
        e.setScaleZ(e.t.scale * t.scale)
      }
      case _ => Unit
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
    transformation
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
   * @param element element, that will be added to View
   * @param x GUI postion
   * @param y GUI postion
   * @return updated element's absolute coordinates
   */
  def initializeInfinityZooming(element: ZoomableElement, x: Double, y: Double): Unit = {
    val e = element
    element.transformation = Distance((x / t.scale) - t.x, (y / t.scale) - t.y, 1 / t.scale)
    e.transformation
  }


}