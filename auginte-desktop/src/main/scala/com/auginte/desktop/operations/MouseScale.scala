package com.auginte.desktop.operations

import javafx.{scene => jfxs}

import com.auginte.desktop.rich.RichNode

import scalafx.scene.input.{MouseEvent, ScrollEvent}

/**
 * Functionality to zoom elements with mouse
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseScale[D <: jfxs.Node] extends MouseZoom[D] {
  @volatile private var mouseDown = false

  mousePressed += {
    (e: MouseEvent) => mouseDown = true
  }

  mouseReleased += {
    (e: MouseEvent) => mouseDown = false
  }

  override protected def zoomed(scale: Double, x: Double, y: Double): Unit = if (mouseDown) scaled(scale)

  protected def scaled(scale: Double): Unit
}
