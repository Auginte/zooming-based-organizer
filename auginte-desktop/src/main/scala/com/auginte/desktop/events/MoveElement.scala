package com.auginte.desktop.events

import javafx.{scene => jfxs}
import com.auginte.desktop.zooming.{ZoomableElement, ZoomableNode}

/**
 * Event of element being moved
 *
 * @param element JavaFx element of Scene hierarchy (cen be delegated from ScalaFx)
 * @param zoomable abstract container saving zooming and translations
 * @param diffX translation in X axis
 * @param diffY translation in Y axis
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class MoveElement(element: jfxs.Node, zoomable: ZoomableElement, diffX: Double, diffY: Double)
  extends ElementEvent with GridEvent
