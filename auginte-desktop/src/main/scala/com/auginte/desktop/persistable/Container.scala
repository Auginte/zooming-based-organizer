package com.auginte.desktop.persistable

import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.RepresentationWrapper
import scalafx.scene.{layout => sfxl}

/**
 * Storing/showing multiple representations.
 */
trait Container { self: sfxl.Pane =>

  def add(representation: RichJPane with RepresentationWrapper): Unit = {
    content.add(representation)
  }

  def remove(representation: RepresentationWrapper): Unit = {
    content.remove(representation)
    representation.storage.remove()
  }
}
