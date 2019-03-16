package com.auginte.desktop.utilities

import java.io.File

import com.typesafe.config.ConfigFactory
import com.typesafe.config.{Config => TypeSafeConfig}

object Config {
  private var conf: TypeSafeConfig = ConfigFactory.parseResources("application.conf")

  def load(path: String): Unit = {
    System.out.println(s"Loading config file: $path")
    this.conf = ConfigFactory.parseFile(new File(path))
  }

  def autoSaveBackups: Boolean = conf.getBoolean("save.backup.enabled")
  def logStorage: Boolean = conf.getBoolean("stats.log.storage")
}
