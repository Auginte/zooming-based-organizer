package com.auginte.distribution.json

import play.api.libs.json.Json

/**
 * Custom properties of storable data.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait DataFormatter {
  val storageFields: Map[String, () => String] = Map()

  def storageJsonConverter = storageFields.map(keyValue => keyValue._1 -> Json.toJson(keyValue._2()))
}
