package com.auginte.common.settings

import java.io.File
import java.util.prefs.Preferences

/**
 * Storing application related settings.
 */
class GlobalSettings {
  private val preferences = Preferences.userNodeForPackage(this.getClass)

  def graphRepository = {
    val repository = GraphRepository(preferences)
    repository.storeTo(preferences)
    repository
  }

  def graphRepository_=(updated: GraphRepository): Unit = updated.storeTo(preferences)

  def clear(): Unit = preferences.clear()

  override def toString: String = List(graphRepository).mkString("\n")
}

object GlobalSettings {
  val productDirectoryName = ".auginte"

  val localGraphRepositoryName = "graphDb"

  def homeDirectory: String = {
    val userHome = System.getProperty("user.home")
    val windowsFallback = System.getProperty("USERPROFILE")
    val currentCurrentDirectory = new File(".").getAbsolutePath
    val prefix = if (userHome != null && userHome != "") {
      userHome
    } else if (windowsFallback != null) {
      windowsFallback
    } else {
      currentCurrentDirectory
    }
    s"$prefix/$productDirectoryName"
  }

  def localGraphDirectory = s"$homeDirectory/$localGraphRepositoryName"
}