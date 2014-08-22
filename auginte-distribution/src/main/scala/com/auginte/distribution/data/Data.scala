package com.auginte.distribution.data

import com.auginte.common.WithId
import com.auginte.zooming.Zoomable
import play.api.libs.json.{JsValue, Json}

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data extends WithId with Zoomable {
  val storageFields: Map[String, () => String] = Map()

  def storageJsonConverter = storageFields.map(keyValue => keyValue._1 -> Json.toJson(keyValue._2()))

  def zoomingJsonConverter: Seq[(String, JsValue)] = List(
    "x" -> Json.toJson(position.x),
    "y" -> Json.toJson(position.y),
    "scale" -> Json.toJson(position.scale),
    "node" -> Json.toJson(s"gn:${node.storageId}")
  )

  val dataType: String = "Abstract"
}