package com.auginte.server.storage

/**
 * Representing version
 */
private[storage] case class Version(_version: String, default: String = "") extends Ordered[Version] {
  private val partSize = 1e3
  private val subVersionsSize = partSize * partSize

  def version: String = if (_version != null && _version != "") _version else default

  def stable: Boolean = !version.endsWith("-SNAPSHOT")

  def numeric: Double = Math.round(toDouble(version) * subVersionsSize) / subVersionsSize

  override def compare(that: Version): Int = ((numeric - that.numeric) * subVersionsSize * partSize).toInt

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