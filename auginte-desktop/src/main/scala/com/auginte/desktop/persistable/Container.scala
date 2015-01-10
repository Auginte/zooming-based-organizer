package com.auginte.desktop.persistable

import com.auginte.distribution.orientdb.RepresentationWrapper
import scalafx.scene.{layout => sfxl}

/**
 * Storing/showing multiple representations.
 */
trait Container { self: sfxl.Pane =>

  def remove(representation: RepresentationWrapper): Unit = {
    content.remove(representation)
    representation.storage.remove()
  }
}
