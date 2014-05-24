package com.auginte.desktop.zooming

import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}

/**
 * Functionality for infinity-zooming enabled JavaFx element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableNode[D <: jfxs.Node] extends RichNode[D] with ZoomableElement {
  //TODO: pass Camera and override translate
}