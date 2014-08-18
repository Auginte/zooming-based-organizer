package com.auginte.distribution.data

import play.api.libs.json.Json

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data {
  /**
   * Unique 32 chars size lower case hex id. E.g. 2cb1a090dd08aecffdb51561ab2b7200
   */
  val storageId: String = f"$longUniqueHexNumber%-32s".replaceAll(" ", "0")
  val storageFields: Map[String, () => String] = Map()

  private lazy val random1 = Math.random() * 1e4
  private lazy val random2 = Math.random() * 1e4
  private lazy val random3 = Math.random() * 1e4
  private lazy val longUniqueHexNumber = f"${System.nanoTime}%h$random1%h$random2%h$random3%h"

  def storageJsonConverter = storageFields.map(keyValue => keyValue._1 -> Json.toJson(keyValue._2()))
}

object Data {
  def apply(id: String) = new Data {
    override val storageId = id
  }

  def unapply(d: Data): Option[String] = Some(d.storageId)
}