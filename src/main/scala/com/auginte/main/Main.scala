package com.auginte.main

import com.auginte.desktop.MainGui

/**
 * Gui independent program launcher.
 *
 * Useful for testing purposes.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object Main {
  def main(args: Array[String]) {
    println("Runing main Auginte project")
    MainGui.main(args)
  }
}
