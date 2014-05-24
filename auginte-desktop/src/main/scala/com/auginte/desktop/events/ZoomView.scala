package com.auginte.desktop.events

import com.auginte.desktop.zooming.ZoomableElement
import javafx.scene.{layout => jfxl}

/**
 * Event that view was zoomed in (`scale` > 1) or out (`scale` < 1)
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ZoomView(zoomable: ZoomableElement, scale: Double, x: Double, y: Double) extends GridEvent
