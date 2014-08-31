package com.auginte.distribution.exceptions

/**
 * Trying to connect elements, but other node by reference id is not found.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class UnconnectedIds(elementsId: String, referenceId: String) extends ImportException {
  override def toString = s"${super.toString}: $elementsId -> $referenceId"
}
