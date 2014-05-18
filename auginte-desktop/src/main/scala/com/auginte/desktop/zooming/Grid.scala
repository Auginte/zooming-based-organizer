package com.auginte.desktop.zooming

import com.auginte.zooming
import com.auginte.zooming.Node

/**
 * Desktop GUI specific grid functionality.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Grid extends zooming.Grid with NodesConnector {
  private var map: Map[_ <: ZoomableNode, Node] = Map()
}
