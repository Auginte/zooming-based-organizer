package com.auginte.zooming

/**
 * Helper for maintaining hierarchy of nodes from absolute positions.
 *
 * Example with grid size 100:
 * {{{
 * Hierarchy     Relative position      Saved
 *   r3          0.012345678 by r3      (0, 0)
 *    ^                                   * 100 +
 *   r2          1.2345678 by r2        (1, 0)
 *    ^                                   * 100 +
 *   r1          123.45678 by r1        (23, 0)
 *    ^                                   * 100 +
 *   root        12345.678 by root      (45, 0)
 *    ^                                   * 100 +
 *   n1          1234567.8 by n1        (67, 0)
 *    ^                                   * 100 +
 *   n2          123456780 by n2        (80, 0)
 *    ^                                   * 100 +
 *   n3          1234567800 by n3       (0, 0)
 * }}}
 *
 * Main principles:
    - Position's precision depends on scaling
    - Every hierarchy level is divided by same square
    - 1 upper level serves for whole lover level square
    - Root node can have both: positive and negative squares
    - Other nodes - only one direction squares
    - Smaller elements go to lower levels
    - Position (x, y) is calculated before scaling

 * {{{
 *   |/_______________/    ||==================||     ||======|======||
 *   |  ______             ||_|       |        ||     ||     _|_     ||
 *   | / , , /             ||         |        ||     ||    | | |    ||
 *   |/_____/              ||         |        ||     --------+-------->
 *   | __                  ||---------+        ||     ||    |_|_|    ||
 *   |/_/                  ||                  ||     ||      |      ||
 *   |                     ||                  ||     ||======|======||
 *   |,                    ||==================||             V
 * }}}
 *
 * For better understanding:
    - For position: Try counting connections between nodes.
      Child-FirstParent, ..., PreParent-LastParent.
      E.g. 123456: LastParent - 56 - PreParent - 34 - Parent(root) - 12 - Child
    - For scaling. Try counting connections between nodes
      E.g. 100000: root - 00 - 00 - 00 - destination
      E.g  1 / 10000: root - 00 - 00 - 00 - destination
 *
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Skeleton(scaleFactor: Int) extends Debugable {
  private var _root = new Node(0, 0)

  private val gridSize = scaleFactor
  private val scaleLog10 = Math.log10(scaleFactor)


  //
  // High abstraction functions
  //

  def root: Node = _root

  /**
   * Gets (creates if needed) closest node.
   *
   * Position is owned by node, if distance is no longer than gridSize.
   * If it is longer, position is related to other nodes.
   * For example:
   * {{{
   *   val gridSize = 100
   *   (0, 0), (99, -99) will be of the same node
   *   (12345, 678) will be divided to node (23, 6) child of (1, 0)
   * }}}
   *
   * Nodes are devided by long distances and large scale differences.
   *
   * @param parent from which node perspective position is needed
   * @param x relative position to node
   * @param y relative position to node
   * @param scale size of new element
   * @return best node for relative transaltion and scaling
   */
  def getNode(parent: Node, x: Double, y: Double, scale: Double): Node = {
    d(s"getNode $parent ${x}x${y}x${scale}")
    if (needOtherNode(x, y, scale)) {
      d(s"from $parent down ${x}x${y}x${scale}")
      getNode(Position(parent, x, y, scale))
    }
    else {
      d(s"Return $parent")
      parent
    }
  }


  //
  // High level best node search function
  //

  @inline
  private def needOtherNode(x: Double, y: Double, scale: Double): Boolean = {
    x.abs >= gridSize || y.abs >= gridSize ||
      Math.log10(scale.abs).abs >= scaleLog10
  }

  /**
   * Abstract algorythm for best node search
   *
   * @param position node, translation and scaling datastore
   * @return
   */
  private def getNode(position: Position): Node = {
    val cleaned = clearTolerance(position)
    d(s"Cleaned: $cleaned")
    val translated = getTranslatedNode(cleaned)
    d(s"Translated: $translated")
    val scaled = getScaledNode(translated)
    d(s"Scaled: $scaled")
    scaled.parent
  }

  /**
   * Part of translation data is ignored, because node owns area around him.
   *
   * Tolerance part must be removed for situation like:
   * {{{
   *   val parent = (1, -2)
   *   val translation = (-0.9, 1.1)
   *   val toleranceValues = (0.9, 0.1)
   *   val cleared = (1 + 0, -2 + 1) = (1, -1)
   * }}}
   */
  private def clearTolerance(pos: Position): Position = {
    def clearTolerance(pos: Position, scale: Double): Position =
      if (isLarger(pos.scale * scale)) {
        d(s"Tolerance:No need: $pos")
        pos
      } else if (isSmaller(pos.scale * scale)) {
        d(s"Tolerance:Smaller: $pos")
        clearTolerance(pos, scale * gridSize)
      } else {
        def clear(value: Double) =
          floor(value * scale / gridSize) * gridSize / scale

        d(s"Tolerance:Applying: $pos | $scale")
        Position(pos.parent, clear(pos.x), clear(pos.y), pos.scale)
      }

    clearTolerance(pos, 1)
  }

  /**
   * Updating position, so there are no translation outside gridSize boundaries.
   *
   * For long translations, parent nodes are created and
   * child nodes are picked from new parents (if needed).
   *
   * Scaling will be unchanged.
   */
  private def getTranslatedNode(position: Position): Position = {
    def diveUp(parent: Node, x: Double, y: Double, scale: Double): Position = {
      d(s"DiveUp $parent | ${x}x${y} s {$scale}")
      if (x.abs >= gridSize || y.abs >= gridSize) {
        val _x = (x / gridSize) + parent.x
        val _y = (y / gridSize) + parent.y
        d(s"\tdiveUp ($x/$gridSize + ${parent.x}, $y/$gridSize + ${parent.y}) = ${_x}x${_y}")
        diveUp(getParent(parent), _x, _y, scale * gridSize)
      } else {
        d(s"\tdived up $parent ${x}x${y} s $scale")
        Position(parent, x, y, scale)
      }
    }

    def diveDown(position: Position, distance: Double): Position = {
      d(s"DiveDown $position | $distance")
      val Position(parent, x, y, scale) = position
      if (distance >= gridSize) {
        val child = getChild(parent, floor(x).toInt, floor(y).toInt)
        val _x = (x * gridSize) - (child.x * gridSize) // Higher precision
        val _y = (y * gridSize) - (child.y * gridSize) // Higher precision
        val _distance = distance / gridSize
        val newPosition = Position(child, _x, _y, scale)
        d(s"DiveDown NEW ${newPosition} (${_x}x${_y})")
        diveDown(newPosition, _distance)
      } else {
        d(s"DiveDown SAME (${x}x${y}/${distance}) -> (${x}x${y}/${scale}")
        Position(parent, x.round, y.round, scale)
      }
    }

    val Position(node, x, y, scale) = position
    val absolute = diveUp(node, x, y, 1)
    val parent = absolute.parent
    d(s"parent = $parent")
    val (_x, _y) = (absolute.x, absolute.y)
    d(s"absolute = ${_x}x${_y}")
    val distance = absolute.scale;
    d(s"distance = $distance (${position.parent} -> ${parent}")
    val translated = diveDown(Position(parent, _x, _y, scale), distance)
    d(s"translated = $translated")
    translated
  }

  /**
   * Updating position, so there are no scaling outside gridSize boundaries.
   *
   * For larger elements, direct nodes directly above will be picked up.
   * For smaller elements, child with best position will be picked up.
   */
  private def getScaledNode(pos: Position): Position =
    if (isLarger(pos.scale)) {
      d(s"Scale:Larger: $pos")
      val parent = getParent(pos.parent)
      val x = pos.x / gridSize + pos.parent.x
      val y = pos.y / gridSize + pos.parent.y
      val scale = pos.scale / gridSize
      getScaledNode(Position(parent, x, y, scale))
    } else if (isSmaller(pos.scale)) {
      d(s"Scale:Smaller: $pos")
      val childX = floor(pos.x % gridSize).toInt
      val childY = floor(pos.y % gridSize).toInt
      val child = getChild(pos.parent, childX, childY)
      d(s" child=${child}")
      val x = (pos.x - childX) * gridSize
      val y = (pos.y - childY) * gridSize
      val scale = pos.scale * scaleFactor
      getScaledNode(Position(child, x, y, scale))
    } else {
      d(s"Scale:Equal: $pos")
      pos
    }


  @inline
  private def isLarger(scale: Double) = Math.log10(scale.abs) >= scaleLog10

  @inline
  private def isSmaller(scale: Double) = Math.log10(scale.abs) <= -scaleLog10

  private def floor(a: Double): Double = if (a >= 0) a.floor else a.ceil


  //
  // Low level node pick up function
  //

  /**
   * Finds or creates (if needed) parent node directly above
   *
   * Root node is also updated
   */
  private def getParent(from: Node): Node = from.parent match {
    case Some(node) => node
    case None => {
      val parent = from.createParent()
      d(s"getParent: ${_root} -> $parent")
      _root = parent
      parent
    }
  }

  /**
   * Finds or crates (if needed) child with spcified relative position
   *
   * Relative position should not go out of gridSize boundaries
   */
  private def getChild(from: Node, x: Int, y: Int): Node =
    from.getChild(x, y) match {
      case Some(node) => node
      case None => {
        val newChild = from.addChild(x, y)
        d(s"getChild: $newChild")
        newChild
      }
    }


  //
  // Utilities
  //

  /**
   * Position data container
   *
   * Node owns gridSize are around him.
   * For example:
   * {{{
   *   val gridSize = 100
   *   val withinRoot = Position(root, 99, 0.12, 1)
   *   val outsideRoot = Position(root, 123, 4567, 1)
   * }}}
   *
   * @param parent node, from which relative position is calculated
   * @param x relative position from node
   * @param y relative position from node
   * @param scale scaling of element
   */
  private case class Position(parent: Node, x: Double, y: Double, scale: Double)

}

trait Debugable {
  protected val DEBUG_MAX_I = 2000
  private var debugI = 0

  def d(text: String = ""): Unit = {
    if (Debug.on) debugString(text)
    debugI += 1
    if (debugI > DEBUG_MAX_I) {
      throw new InfinityRecursion
    }
  }

  protected def debugString(text: String) = {
    if (text.length > 0) println(text)
    try {
      val stack = Thread.currentThread().getStackTrace()
      val element = stack(5)
      println(s"\t$element")
    } catch {
      case e: Exception => println("Stack trace went wrong")
    }
  }

  class InfinityRecursion() extends RuntimeException("Infinity recursion")

}

//FIXME: debug
object Debug {
  var on: Boolean = false

  def print(text: String): Unit = if (on) println(s"==========$text==========")
}