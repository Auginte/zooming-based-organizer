package com.auginte.distribution.exceptions

import com.auginte.distribution.data.Version

/**
 * Trying to read file with unexpected element location
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class UnsupportedStructure[A](expectedType: Class[A], element: Any) extends ImportException
