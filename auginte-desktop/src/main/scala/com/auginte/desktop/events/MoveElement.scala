package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event of element being moved
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class MoveElement(element: jfxs.Node, diffX: Double, diffY: Double) extends ElementEvent
