package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event that element was updated, repainting is needed.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ElementUpdated(element: jfxs.Node) extends ElementEvent
