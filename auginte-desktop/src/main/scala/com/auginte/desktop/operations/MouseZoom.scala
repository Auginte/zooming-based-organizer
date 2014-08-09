package com.auginte.desktop.operations

import javafx.{scene => jfxs}

import com.auginte.desktop.rich.RichNode

import scalafx.scene.input.ScrollEvent

/**
 * Functionality to zoom elements with mouse
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait MouseZoom[D <: jfxs.Node] extends RichNode[D] with Zoom {
  scrolled += ((e: ScrollEvent) => if (e.deltaY != 0) zoomed(delta2scale(e.deltaY), e.x, e.y))
}
