package com.auginte.desktop.events

import javafx.{scene => jfxs}

/**
 * Event to remove node from panel or other data containers
 */
case class DeleteElement(element: jfxs.Node) extends ElementEvent
