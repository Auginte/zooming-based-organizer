package com.auginte.distribution.data

/**
 * Encapsulating version.
 *
 * `-SNAPSHOT` keyword is used to mark code, that can change without version number being changed.
 *
 * @param version E.g. 1.2.3 4.5.8-SNAPSHOT 0.0.0 0.0.1 99.99.99-SNAPSHOT
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Version(val version: String) {
  private val partSize = 1e3
  private val subVersionsSize = partSize * partSize

  def stable: Boolean = !version.endsWith("-SNAPSHOT")

  def <=(another: Version): Boolean = this < another || this == another

  def >(another: Version): Boolean = numeric > another.numeric

  def numeric: Double = Math.round(toDouble(version) * subVersionsSize) / subVersionsSize

  def >=(another: Version): Boolean = this > another || this == another

  def <(another: Version): Boolean = numeric < another.numeric

  private def toDouble(version: String): Double = if (version.endsWith("-SNAPSHOT")) {
    toDouble(version.split("-SNAPSHOT")(0))
  } else {
    val parts = version.split("\\D+")
    assume(parts.size == 3, s"Not valid format for version: $version ${parts.toList}. Expected 0.1.2")
    parts(0).toDouble + (parts(1).toDouble / partSize) + (parts(2).toDouble / subVersionsSize)
  }

  override def hashCode(): Int = version.hashCode()

  override def equals(obj: scala.Any): Boolean = obj match {
    case v: Version => v.version.equals(version)
    case _ => false
  }

  override def toString: String = version
}
object Version {
  def apply(version: String) = new Version(version)

  def unapply(v: Version): Option[String] = Some(v.version)
}