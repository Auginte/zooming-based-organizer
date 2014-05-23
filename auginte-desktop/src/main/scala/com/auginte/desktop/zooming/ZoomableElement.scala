package com.auginte.desktop.zooming

import com.auginte.zooming.Distance

/**
 * General purpose element with infinity zooming parameters.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableElement {
  var transformation: Distance = new Distance()
}
