package com.auginte.zooming

import scala.annotation.tailrec
import scala.Some

/**
 * Class to ensure infinity scaling and translation.
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
abstract class Grid extends Debugable {
  private var _root = new Node(0, 0)

  /**
   * Distance between nodes.
   */
  val gridSize: Int = 100

  /**
   * When calculating new absolute position, approximation used.
   */
  val absolutePrecision = 1000000

  private lazy val scaleLog10 = Math.log10(gridSize)

  //
  // High abstraction functions
  //

  def root: Node = _root

  /**
   *
   * [[getNode]]
   */
  def getNode(parent: Node, absolute: Distance): Node = getNode(parent, absolute.x, absolute.y, absolute.scale)

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

  /**
   * Textual representation of absolute coordinates from same level child perspective.
   *
   * Used to test very long distances/scales (more thant double can handle).
   * For example:
   * {{{
   *   r3(12, 9) <- r2(34, 0) <- r1(56, 1)
   *   ("123456", "90001", "10000")
   *
   *
   *   root
   *   | \_r3(12, 9)
   *   |     \_r2(34, 0)
   *   from----> \_r1(56, 1)
   * }}}
   *
   * @throws IllegalArgumentException When there are no child-parent relation
   * @see [[absoluteChild]]
   */
  def absoluteTextual(child: Node, parent: Node): TextualCoordinates = if (child == parent) ("0", "0", "1")
  else {
    require(child.isChildOf(parent), s"Not parent-child: $parent $child")
    d(s"getCoordinates $parent - $child")

    type TC = TextualCoordinates
    val originalChild = child

    val scale = gridSize.toString().substring(1)
    val format = (number: Int) => number.formatted("%0" + scale.length + "d")
    val leaveLastZero = (s: String) => if (s.length > 0) s else "0"
    val strip = (s: String) => leaveLastZero(s.dropWhile(_ == '0'))
    val stripped = (c: TC) => (strip(c._1), strip(c._2), c._3)
    val newCoordinates = (node: Node, c: TC) =>
      (format(node.x) + c._1, format(node.y) + c._2, c._3 + scale)

    def appended(child: Node, coordinates: TC, end: Boolean): TC = {
      d(s" appended $child: $coordinates")
      if (end) {
        require(child eq parent, s"Not parent-child: $parent $originalChild")
        return stripped(coordinates)
      }
      child.parent match {
        case Some(node) =>
          appended(node, newCoordinates(node, coordinates), node eq parent)
        case None => appended(child, coordinates, true)
      }
    }

    appended(child, (format(child.x), format(child.y), "1"), false)
  }

  /**
   * Absolute coordinates from same level child perspective.
   *
   * Scale parameter shows scale distance from child to parent.
   *
   * For example:
   * {{{
   *   r3(12, 9) <- r2(34, 0) <- r1(56, 1)
   *   (123456, 90001, 10000)
   *
   *   root
   *   | \_r3(12, 9)
   *   |     \_r2(34, 0)
   *   from----> \_r1(56, 1)
   * }}}
   * @throws IllegalArgumentException When there are no child-parent relation
   * @see [[absoluteTextual]]
   */
  def absoluteChild(child: Node, parent: Node): Distance = if (child == parent) Distance(0, 0, 1)
  else {
    require(child.isChildOf(parent), s"Not parent-child: $parent - $child")
    @inline
    def newScale(d: Distance) = d.scale * gridSize
    @inline
    def newDistance(n: Node, d: Distance) = Distance((n.x * newScale(d)) + d.x, (n.y * newScale(d)) + d.y, newScale(d))
    @tailrec
    def absolute(node: Node, last: Node, distance: Distance): Distance = node.parent match {
      case Some(parent) if node != last => absolute(parent, last, newDistance(parent, distance))
      case _ => distance
    }
    absolute(child, parent, Distance(child.x, child.y, 1))
  }

  /**
   * Absolute coordinates for node from Graphical user interface perspective (including camera postion).
   *
   * Transformations:
   * {{{
   *   G_p = (C_p + E_p) * C_s
   *   G_s = C_s * E_s
   *   // G_p - Graphical user interface (GUI, e.g. JavaFx) position
   *   // C_p - Camera's absolute position
   *   // E_p - Element's own absolute position
   *   // C_s - Camera's absolute scale (inverse of zooming)
   *   // E_s - Element's absolute scale
   *   // G_s - GUI scale of element (as it appears to user)
   * }}}
   */
 def absoluteNode(camera: Node, cameraPosition: Distance,  element: Node, elementPosition: Distance): Distance =
  if (camera == element) {
    val cp = cameraPosition
    val ep = elementPosition
    Distance((cp.x + ep.x) * cp.scale, (cp.y + ep.y) * cp.scale, cp.scale * ep.scale)
  } else {
    //TODO: no covered by tests
    val parent = getCommonParent(camera, element)
    val absoluteFrom = absoluteChild(camera, parent)
    val absoluteTo = absoluteChild(element, parent)
    absoluteNode(element, (absoluteTo -- absoluteFrom) ++ cameraPosition, element, elementPosition)
  }

  /**
   * Generating new absolute postion for new node, using GUI coordinates
   */
  def absoluteNew(cameraPosition: Distance, x: Double, y: Double): Distance = {
    val p = cameraPosition
    Distance((x / p.scale) - p.x, (y / p.scale) - p.y, 1 / p.scale)
  }


  /**
   * Calculates new absolute coordinates after node change.
   *
   * @param from previous node
   * @param to new node
   * @param oldPosition previous absolute position
   * @return new absolute position from new child perspective
   */
  def absoluteCamera(from: Node, to: Node, oldPosition: Distance): Distance = {
    val parent = getCommonParent(from, to)
    d(s"Common parent: $parent")
    val absoluteFrom = absoluteChild(from, parent)
    d(s"absoluteFrom: $absoluteFrom")
    val absoluteTo = absoluteChild(to, parent)
    d(s"absoluteTo: $absoluteTo")
    val result = if (absoluteFrom.scale == absoluteTo.scale) {
      val difference = (absoluteTo -- absoluteFrom) normalised 1 zoomed oldPosition.scale
      d(s"difference EQ: $difference")
      oldPosition -- difference
    } else {
      val factor = absoluteTo.scale / absoluteFrom.scale
      d(s"factor: $factor")
      val difference = (absoluteTo -- absoluteFrom) normalised 1 zoomed oldPosition.scale
      d(s"difference DIFF SCALE: $difference")
      val diff2 = oldPosition -- difference
      d(s"diff2 DIFF SCALE: $diff2")
      diff2 * factor
    }
    d(s"result: $result")
    result
  }

  private def getCommonParent(from: Node, to: Node): Node = {
    def whileSame(nodes1: List[Node], nodes2: List[Node], same: Node): Node = {
      nodes1 match {
        case head :: tail if !nodes2.isEmpty && head == nodes2.head => whileSame(tail, nodes2.tail, head)
        case List(head) if !nodes2.isEmpty && head == nodes2.head => head
        case _ => same
      }
    }
    val parentsFrom = from.selfAndParents
    val parentsTo = to.selfAndParents
    whileSame(parentsFrom, parentsTo, parentsFrom.head)
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
      val scale = pos.scale * gridSize
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
  protected val DEBUG_MAX_I = 2000000
  private var debugI = 0

  def d(text: String = ""): Unit = {
    if (Debug.on) debugString(text)
    debugI += 1
//    if (debugI > DEBUG_MAX_I) {
    //      throw new InfinityRecursion
    //    }
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