package com.auginte.desktop.zooming

import com.auginte.zooming.Node

/**
 * Functionality related to using Grid (core element for infinity zooming)
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait UsingGrid {
  private var gridOption: Option[Grid] = None
  private var nodeOption: Option[Node] = None

  def grid_=(g: Grid): Unit = gridOption = Some(g)

  def grid: Grid = if (gridOption.isDefined) {
    gridOption.get
  } else {
    throw new IllegalArgumentException("using nodes connector without grid initiated")
  }

  def node_=(n: Node): Unit = nodeOption = Some(n)

  def node: Node = if (nodeOption.isDefined) {
    nodeOption.get
  } else {
    throw new IllegalArgumentException("using nodes connector without node assigned")
  }
}
