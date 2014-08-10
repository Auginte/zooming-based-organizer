package com.auginte

import java.util.Properties

/**
 * Class to deal with software versions.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object Version {
  val fallBackVersion = "0.0.1-SNAPSHOT"

  private lazy val properties: Option[Properties] = {
    val buildProperties = new Properties()
    try {
      buildProperties.load(getClass.getResourceAsStream("build.properties"))
      Some(buildProperties)
    } catch {
      case e: Exception => None
    }
  }

  override def toString: String = properties match {
    case Some(p) if p.getProperty("version") != null => p.getProperty("version")
    case _ => fallBackVersion
  }

  def toDouble(version: String): Double = if (version.endsWith("-SNAPSHOT")) {
    toDouble(version.split("-SNAPSHOT"){0})
  } else {
    val parts = version.split("\\D+")
    assume(parts.size == 3, s"Not valid format for version: $version ${parts.toList}. Expected 0.1.2")
    parts{0}.toDouble + (parts{1}.toDouble * 1e-3) + (parts{2}.toDouble * 1e-6)
  }
}
