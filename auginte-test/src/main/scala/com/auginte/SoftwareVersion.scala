package com.auginte

import java.util.Properties

/**
 * Class to deal with this software versions.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object SoftwareVersion {
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
}
