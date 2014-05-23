package com.auginte.desktop.zooming

import com.auginte.zooming
import com.auginte.zooming.Node
import javafx.{scene => jfxs}

/**
 * Functionality to connect Desktop elements to Grid  and vice versa.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait NodesConnector[D <: jfxs.Node] extends zooming.Grid {
  private var map: Map[_ <: ZoomableNode[D], Node] = Map()
}
