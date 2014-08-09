package com.auginte.desktop.actors

import javafx.{scene => jfxs}

import com.auginte.desktop.events.ElementScaled
import com.auginte.desktop.operations.MouseScale
import com.auginte.desktop.zooming.ZoomableNode

import scalafx.scene.input.MouseEvent

/**
 * Delegating every scale event to View as event.
 * 
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ScalableElement[D <: jfxs.Node] extends MouseScale[D] with ViewableNode with ZoomableNode[D] {
  override protected def scaled(scale: Double): Unit = {
    view ! ElementScaled(d, this, scale)
  }
}
