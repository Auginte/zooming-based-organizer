package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event of view being moved.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class MoveView(view: jfxs.layout.Pane, diffX: Double, diffY: Double) extends ViewEvent
