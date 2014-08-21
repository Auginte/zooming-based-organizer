package com.auginte.distribution.data

import com.auginte.common
import play.api.libs.json.Json

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data extends common.Data {
  val storageFields: Map[String, () => String] = Map()

  def storageJsonConverter = storageFields.map(keyValue => keyValue._1 -> Json.toJson(keyValue._2()))
}

object Data {
  def apply(id: String) = new Data {
    override val storageId = id
  }

  def unapply(d: Data): Option[String] = Some(d.storageId)
}