package com.auginte.distribution.data

import com.auginte.zooming.{Node, Coordinates}

/**
 * Place holder for [[Camera]],
 * so relations could be updated after all elements are loaded.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ImportedCamera(id: String, x: Double, y: Double, scale: Double, nodeId: String) extends Camera {
  override def position: Coordinates = Coordinates(x, y, scale)

  override def node: Node = nodePlaceHolder

  val nodePlaceHolder = Node(0, 0)
}
