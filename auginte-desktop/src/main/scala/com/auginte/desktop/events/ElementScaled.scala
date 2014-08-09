package com.auginte.desktop.events

import javafx.{scene => jfxs}

import com.auginte.desktop.zooming.ZoomableElement

/**
 * Event that element scale was changed.
 * 
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ElementScaled(element: jfxs.Node, zoomable: ZoomableElement, scale: Double)
  extends ElementEvent with GridEvent