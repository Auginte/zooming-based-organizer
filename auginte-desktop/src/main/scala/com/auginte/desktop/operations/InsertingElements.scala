package com.auginte.desktop.operations

import javafx.scene.{input => jfxi}
import javafx.{collections => jfxc, scene => jfxs}

import com.auginte.desktop.nodes.Label
import com.auginte.desktop.rich.RichNode

import scalafx.scene.{input => sfxi}

/**
 * Generalisation of operators with elements insert functionality.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait InsertingElements[D <: jfxs.Node] extends RichNode[D] {
  def children: jfxc.ObservableList[jfxs.Node]

  protected def createNewElement: jfxs.Node = new Label()

  protected def insertElement(element: jfxs.Node, x: Double, y: Double): Unit = {
    element.setTranslateX(x)
    element.setTranslateY(y)
    children.add(element)
  }
}
