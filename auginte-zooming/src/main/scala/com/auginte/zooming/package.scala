package com.auginte

import com.auginte.common.WithParentId

/**
 * Package is responsible for infinity zooming,
 * by preserving hierarchy of nodes and
 * proving zooming/translation related helpers.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
package object zooming {
  /**
   * (X, Y, Scale) in textual representation.
   * E.g. ("1234567890123", "12345678", "124567891023456789")
   */
  type TextualCoordinates = (String, String, String)

  /**
   * Data structure to fully describe element's location in infinity zooming space.
   */
  type GlobalCoordinates = (Node, Coordinates)

  /**
   * Dependency injection for new node creation
   */
  type NodeToNode = (Node) => Node

  /**
   * Type to mark nodes, that does not have real relations to other nodes.
   */
  type ImportedNode = Node with WithParentId

  /**
   * Default implementation for [[NodeToNode]] injection
   */
  val sameNode: NodeToNode = (n) => n

  /**
   * Relating imported node's id to node as an object.
   */
  type IdToRealNode = Map[String, Node]
}
