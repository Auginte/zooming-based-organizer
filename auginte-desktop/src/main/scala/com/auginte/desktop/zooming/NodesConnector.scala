package com.auginte.desktop.zooming

import com.auginte.zooming
import com.auginte.zooming.Node

/**
 * Functionality to connect Desktop elements to Grid  and vice versa.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait NodesConnector extends zooming.Grid {
  private var map: Map[_ <: ZoomableNode, Node] = Map()
}
