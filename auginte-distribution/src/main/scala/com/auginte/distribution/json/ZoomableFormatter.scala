package com.auginte.distribution.json

import com.auginte.zooming.Zoomable
import play.api.libs.json.{Json, JsValue}

/**
 * JSON converters for [[Zoomable]] elements.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ZoomableFormatter extends Zoomable {
  def zoomingJsonConverter: Seq[(String, JsValue)] = List(
    "x" -> Json.toJson(position.x),
    "y" -> Json.toJson(position.y),
    "scale" -> Json.toJson(position.scale),
    "node" -> Json.toJson(s"gn:${node.storageId}")
  )
}
