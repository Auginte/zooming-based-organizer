package com.auginte.desktop.events

import javafx.{scene => jfxs}
import com.auginte.desktop.zooming.{ZoomableElement, ZoomableCamera}
import javafx.scene.{layout => jfxl}

/**
 * Event of view being moved.
 *
 * @param view JavaFx panel having actual elements (usually delegated from ScalaFx)
 * @param zoomable abstract container saving zooming and translations
 * @param diffX translation in X axis
 * @param diffY translation in Y axis
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class MoveView(view: jfxl.Pane, zoomable: ZoomableElement, diffX: Double, diffY: Double)
  extends ViewEvent with GridEvent
