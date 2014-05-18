package com.auginte.desktop.zooming

import com.auginte.zooming.Distance

/**
 * Zooming related functionality for containers.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableCamera {
  var transformation: Distance = new Distance()
}
