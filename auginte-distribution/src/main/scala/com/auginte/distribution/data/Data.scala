package com.auginte.distribution.data

/**
 * Common functionality for objects, that can be saved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Data {
  private val random1 = Math.random() * 1e4
  private val random2 = Math.random() * 1e4
  private val random3 = Math.random() * 1e4
  private val longUniqueHexNumber = f"${System.nanoTime}%h$random1%h$random2%h$random3%h"

  /**
   * Unique 32 chars size lower case hex id. E.g. 2cb1a090dd08aecffdb51561ab2b7200
   */
  val storageId: String = f"$longUniqueHexNumber%-32s".replaceAll(" ", "0")
}
object Data {
  def apply(id: String) = new Data {
    override val storageId = id
  }

  def unapply(d: Data): Option[String] = Some(d.storageId)
}