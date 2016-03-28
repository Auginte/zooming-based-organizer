package com.auginte.desktop.persistable

import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.RepresentationWrapper
import scalafx.scene.{layout => sfxl}

/**
 * Storing/showing multiple representations.
 */
trait Container { self: sfxl.Pane =>

  def add(representation: RichJPane with RepresentationWrapper): Unit = {
    children.add(representation)
  }

  def remove(representation: RepresentationWrapper): Unit = {
    children.remove(representation)
    representation.storage.remove()
  }
}
