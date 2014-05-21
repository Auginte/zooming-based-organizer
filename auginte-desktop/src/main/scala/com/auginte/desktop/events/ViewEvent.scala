package com.auginte.desktop.events

import javafx.scene.{layout => jfxl}

/**
 * Events using View as element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ViewEvent {
  val view: jfxl.Pane
}
