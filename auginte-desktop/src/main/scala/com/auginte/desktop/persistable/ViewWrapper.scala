package com.auginte.desktop.persistable

import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.{scene => jfxs}

import com.auginte.desktop.rich.RichNode
import com.auginte.distribution.orientdb.RepresentationWrapper

/**
 * Gets persistable elements from JavaFX: Zoomable node and it's camera
 */
trait ViewWrapper {
  private var _view: Option[View] = None
  
  def view = _view
  
  def view_=(view: View): Unit = _view = Some(view)
}
