package com.auginte.desktop.persistable

import javafx.{scene => jfxs}
import com.auginte.desktop.operations

trait MouseZoom[D <: jfxs.Node] extends operations.MouseZoom[D] with View {
  override protected def zoomed(scale: Double, x: Double, y: Double): Unit = {
    zoom(scale, x, y)
    updateCachedToDb()
  }
}
