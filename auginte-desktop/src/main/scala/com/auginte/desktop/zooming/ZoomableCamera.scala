package com.auginte.desktop.zooming

import javafx.scene.{layout => jfxl}
import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}
import scala.collection.JavaConversions._
import com.auginte.zooming.Distance

/**
 * Zooming related functionality for containers.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera[D <: jfxl.Pane] extends RichNode[D] with ZoomableElement {
  /**
   * For every child, converts infinity zooming coordinates to JavaFx ones
   */
  protected def absoluteToCachedCoordinates(): Unit = {
    for (element <- d.getChildren) element match {
      case e: ZoomableNode[D] => {
        e.setTranslateX(transformation.x + e.transformation.x)
        e.setTranslateY(transformation.y + e.transformation.y)
      }
      case _ => Unit
    }
  }

  /**
   * Initiates absolute transformation by current camera transformation.
   *
   * @param element element, that will be added to View
   * @param x position from user perspective
   * @param y position from user perspective
   */
  def translateNew(element: ZoomableElement, x: Double, y: Double): Unit =
    element.transformation = Distance(x - transformation.x, y - transformation.y, 1)

}