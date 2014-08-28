package com.auginte.zooming

/**
 * Test helpers for comparing [[com.auginte.zooming.Node]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait NodeAssertions {
  def assertXY(node: Node, x: Int, y: Int): Unit =
    assert(node.x == x && node.y == y,
      s"Expected ${x}x$y, but actual ${node.x}x${node.y} in $node\n")


  def assertXY(expected: Node, node: Node): Unit = assertXY(node, expected.x, expected.y)

  def assertXY(expected: List[Node], nodes: List[Node]): Unit = (expected, nodes).zipped.foreach(assertXY)
}
