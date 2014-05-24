package com.auginte.desktop.zooming

import com.auginte.zooming
import com.auginte.zooming.Node
import javafx.{scene => jfxs}
import javafx.scene.{Node => jn}
import com.auginte.zooming.Node

/**
 * Functionality to connect Desktop elements to Grid  and vice versa.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Grid extends zooming.Grid {
  type GridMap = Map[jn, Node]

  //FIXME: revert to protected[zooming]
  protected[desktop] var map: GridMap = Map()
}
