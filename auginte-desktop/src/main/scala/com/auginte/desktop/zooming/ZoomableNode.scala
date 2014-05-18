package com.auginte.desktop.zooming

import com.auginte.zooming.Distance

/**
 * Functionality for infinity-zooming enabled element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableNode {
  var transformation: Distance = new Distance()
}
