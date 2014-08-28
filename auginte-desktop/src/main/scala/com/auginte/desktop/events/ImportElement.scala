package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event to insert imported element to view.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ImportElement(element: jfxs.Node) extends ElementEvent
