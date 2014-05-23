package com.auginte.desktop.events

import javafx.scene.{layout => jfxl}
import com.auginte.desktop.zooming.ZoomableElement

/**
 * Events related to infinity zooming.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait GridEvent {
  val zoomable: ZoomableElement
}
