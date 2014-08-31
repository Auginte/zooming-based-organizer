package com.auginte.distribution.exceptions

import com.auginte.distribution.data.Version

/**
 * Trying to read element with unexpected fields
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class UnsupportedElement(previous: Exception) extends ImportException {
  override def toString = s"${super.toString}: $previous"
}
