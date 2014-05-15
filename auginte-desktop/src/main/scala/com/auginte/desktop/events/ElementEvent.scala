package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Events using elements of the View.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ElementEvent {
  val element: jfxs.Node
}
