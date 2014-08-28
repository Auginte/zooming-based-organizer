package com.auginte.distribution.exceptions

import com.auginte.distribution.data.Version

/**
 * Trying to read element with unexpected fields
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class UnsupportedElement(e: Exception) extends ImportException
