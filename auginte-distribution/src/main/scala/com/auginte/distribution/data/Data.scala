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
  private val uniqueId = f"${System.nanoTime}%h$random1%h$random2%h$random3%h"

  /**
   * @return unique 32 chars size lower case hex id. E.g. 2cb1a090dd08aecffdb51561ab2b7200
   */
  def id: String = f"$uniqueId%-32s".replaceAll(" ", "0")
}