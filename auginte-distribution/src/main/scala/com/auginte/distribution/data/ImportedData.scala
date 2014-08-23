package com.auginte.distribution.data

import com.auginte.zooming.{Node, Distance}

/**
 * Place holder for Graphical user interface implementations of Data container
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ImportedData(
                         id: String,
                         typeName: String,
                         x: Double,
                         y: Double,
                         scale: Double,
                         nodeId: String,
                         customFields: Traversable[(String, String)]
                         ) extends Data {
  override def position: Distance = Distance(x, y, scale)

  override def node: Node = nodePlaceHolder

  val nodePlaceHolder = Node(0, 0)
}
