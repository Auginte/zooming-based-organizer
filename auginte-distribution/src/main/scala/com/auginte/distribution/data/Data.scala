package com.auginte.distribution.data

import com.auginte.common.WithId
import com.auginte.distribution.json.{TransformableFormatter, ZoomableFormatter, DataFormatter}
import com.auginte.transforamtion.Descendant
import com.auginte.zooming.Zoomable

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data extends WithId with DataFormatter
with ZoomableFormatter
with TransformableFormatter {

  val dataType: String = "Abstract"

}