package com.auginte.common

/**
 * Centralised place for user level/runtime exceptions
 *
 * @see [[scala.Predef.assume]]
 */
object Unexpected {
  def state(message: String) = throw new AssertionError(message)
}
