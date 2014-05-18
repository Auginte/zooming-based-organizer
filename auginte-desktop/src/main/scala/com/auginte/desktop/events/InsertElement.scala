package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event to add new element to
 */
case class InsertElement(element: jfxs.Node, x: Double, y: Double) extends ElementEvent
