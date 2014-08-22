package com.auginte.distribution.data

import com.auginte.common.WithId
import com.auginte.distribution.json.{ZoomableFormatter, DataFormatter}
import com.auginte.zooming.Zoomable

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data extends WithId with DataFormatter with ZoomableFormatter with Zoomable {
  val dataType: String = "Abstract"
}