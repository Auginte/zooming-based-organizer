package com.auginte.desktop.persistable

import javafx.{scene => jfxs}
import com.auginte.common.Unexpected
import com.auginte.desktop.operations
import com.auginte.distribution.orientdb.RepresentationWrapper


/**
 * Scaling and persisting element. 
 */
trait MouseScale[D <: jfxs.Node] extends operations.MouseScale[D]
  with RepresentationWrapper with ViewWrapper{
  override protected def scaled(scale: Double): Unit = view match {
    case Some(view) => view.scale(this, scale)
    case _ => Unexpected.state(s"Zooming element without view assigned: $this")
  }
}
