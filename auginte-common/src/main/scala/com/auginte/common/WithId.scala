package com.auginte.common

/**
 * Common functionality for objects, that have unique id,
 *
 * Is is used for storage/serialisation.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait WithId {
  /**
   * Unique 32 chars size lower case hex id. E.g. 2cb1a090dd08aecffdb51561ab2b7200
   */
  val storageId: String = f"$longUniqueHexNumber%-32s".replaceAll(" ", "0")

  private lazy val random1 = Math.random() * 1e4
  private lazy val random2 = Math.random() * 1e4
  private lazy val random3 = Math.random() * 1e4
  private lazy val longUniqueHexNumber = f"${System.nanoTime}%h$random1%h$random2%h$random3%h"
}