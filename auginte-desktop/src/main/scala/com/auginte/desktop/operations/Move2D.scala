package com.auginte.desktop.operations

import javafx.{scene => jfxs}

import com.auginte.desktop.rich.RichNode

/**
 * Operations related to moving element.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Move2D[D <: jfxs.Node] extends RichNode[D] {
  def saveDraggedPosition(diffX: Double, diffY: Double): Unit = {
    d.setTranslateX(d.getTranslateX + diffX)
    d.setTranslateY(d.getTranslateY + diffY)
  }
}
