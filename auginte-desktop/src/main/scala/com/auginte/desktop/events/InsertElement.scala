package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event for user to add new element to view
 */
case class InsertElement(element: jfxs.Node, x: Double, y: Double) extends ElementEvent
