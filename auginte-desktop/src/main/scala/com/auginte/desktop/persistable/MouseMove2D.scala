package com.auginte.desktop.persistable

import javafx.{scene => jfxs}

import com.auginte.common.Unexpected
import com.auginte.desktop.operations
import com.auginte.distribution.orientdb.RepresentationWrapper

import scalafx.scene.input.MouseEvent

trait MouseMove2D[D <: jfxs.Node] extends operations.MouseMove2D[D]
  with RepresentationWrapper with ViewWrapper {

  override def saveDraggedPosition(diffX: Double, diffY: Double): Unit = view match {
    case Some(view) => view.translate(this, diffX, diffY)
    case _ => Unexpected.state(s"Dragging element without view assigned: $this")
  }
}
