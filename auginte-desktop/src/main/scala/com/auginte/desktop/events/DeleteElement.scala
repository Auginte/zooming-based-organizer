package com.auginte.desktop.events

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}

/**
 * Event to remove node from panel or other data containers
 */
case class DeleteElement(element: jfxs.Node)
