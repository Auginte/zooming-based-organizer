package com.auginte.distribution.exceptions

import com.auginte.distribution.data.Version

/**
 * Trying to read file with unsupported version.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class UnsupportedVersion(fileVersion: Version, neededVersion: Version) extends ImportException {
  override def toString = s"${super.toString}: $fileVersion, but needed $neededVersion"
}
