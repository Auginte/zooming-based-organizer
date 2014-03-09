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
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Skeleton(scaleFactor: Int) extends Debugable {
  private var _root = new Node(0, 0)

  private val gridSize = scaleFactor
  private val scaleLog10 = Math.log10(scaleFactor)

  // High abstraction functions

  def root: Node = _root

  def getNode(from: Node, x: Double, y: Double, scale: Double): Node = {
    d(s"getNode $from ${x}x${y}x${scale}")
    val (_x, _y) = getNormalised(Position(from, x, y, scale))
    d(s"Normalised: ${_x}x${_y}")
    if (needOtherNode(x, y, scale)) {
      d(s"from $from down ${x}x${y}x${scale}")
      getNode(Position(from, x, y, scale))
    }
    else {
      d(s"Return $from")
      from
    }
  }

  /**
   * @deprecated negative position in translation
   */
  private def getNormalised(position: Position): (Double, Double) = {
    def absoluteSign(node: Node, sum: Double, param: Node => Double): Double =
      node.parent match {
        case Some(parent) => {
          d(s"  normalising $node: $sum")
          absoluteSign(parent, sum / gridSize + param(parent), param)
        }
        case None => sum
      }

    val (node, _x, _y) = (position.node, position.x, position.y)
    val absoluteX = absoluteSign(node, _x, node => node.x)
    val absoluteY = absoluteSign(node, _y, node => node.y)

    val x: Double = if (isSameSign(absoluteX, _x)) _x else gridSize - _x
    val y: Double = if (isSameSign(absoluteY, _y)) _y else gridSize - _y

    d(s"normalised (${_x}, ${_y}) -> ($x, $y)")

    (x, y)
  }

  /**
   * @deprecated debug only
   */
  def absoluteSign(node: Node, sum: Double, param: Node => Double): Double =
    node.parent match {
      case Some(parent) => {
        var p = parent
        println(s"$node -> $p: $sum")
        absoluteSign(parent, sum / gridSize + param(parent), param)
      }
      case None => sum
    }

  @inline
  private def isSameSign(a: Double, b: Double) =
    (a >= 0 && b >= 0) || (a < 0 && b < 0)

  /**
   * Optimisation for close nodes
   */

  @inline
  private def needOtherNode(x: Double, y: Double, scale: Double): Boolean = {
    x.abs >= gridSize || y.abs >= gridSize ||
      Math.log10(scale.abs).abs >= scaleLog10
  }

  private def getNode(position: Position): Node = {
    val translated = getTranslatedNode(position)
    d(s"Translated: $translated")
    val scaled = getScaledNode(translated)
    d(s"Scaled: $scaled")
    scaled.node
  }


  private def getTranslatedNode(position: Position): Position = {
    def getAbsolute(pos: Position): Position = {
      pos
    }

    def diveUp(child: Node, x: Double, y: Double): Node = {
      d(s"DiveUp $child | ${x}x${y}")
      val _x = (x - child.x)
      val _y = (y - child.y)
      d(s"\tdiveUp ($x - ${child.x}, $y - ${child.y}) = ${_x}x${_y}")
      if (x.abs >= gridSize && y.abs >= gridSize) {
        diveUp(getParent(child), _x / gridSize, _y / gridSize)
      } else if (x.abs >= gridSize && y.abs < gridSize) {
        diveUp(getParent(child), _x / gridSize, _y)
      } else if (x.abs < gridSize && y.abs >= gridSize) {
        diveUp(getParent(child), _x, _y / gridSize)
      } else {
        d(s"\tdived up ${_x}x${_y}")
        child
      }
    }

    def getDistance(child: Node, parent: Node, value: Double = 1): Double = {
      d(s"getDistance $child -> $parent | $value")
      if (child != parent) {
        child.parent match {
          case Some(node) if (node == parent) => {
            d(s"getDistance REACHED parent [${value}] ${node}")
            value * gridSize
          }
          case Some(node) if (node != parent) => {
            d(s"getDistance NOT parent [${value}] ${node}")
            getDistance(node, parent, (value * gridSize).floor)
          }
          case _ => {
            d(s"getDistance node-NONE [${value}] ${child.parent}")
            value
          }
        }
      } else {
        d(s"getDistance child-PARENT [${value}] ${child}")
        value
      }
    }

    def diveDown(position: Position, distance: Double): Position = {
      d(s"DiveDown $position | $distance")

      val Position(parent, x, y, scale) = position
      val _x = if (x.abs >= gridSize) floor(x / distance) % gridSize else x
      val _y = if (y.abs >= gridSize) floor(y / distance) % gridSize else y
      val _distance = distance / gridSize
      if (distance >= gridSize) {
        val child = getChild(parent, _x.toInt, _y.toInt)
        val newPosition = Position(child, x, y, scale)
        d(s"DiveDown NEW (${_x}x${_y}/${_distance}: ${newPosition}")
        diveDown(newPosition, _distance)
      } else {
        d(s"DiveDown SAME (${x}x${y}/${distance}) -> (${_x}x${_y}/${_distance}")
        Position(parent, _x, _y, scale)
      }
    }

    val parent = diveUp(position.node, position.x, position.y)
    d(s"parent = $parent")
    val distance = getDistance(position.node, parent)
    d(s"distance = $distance (${position.node} -> ${parent}")
    val translated = diveDown(Position(parent, position.x, position.y, position.scale), distance)
    d(s"translated = $translated")
    translated
  }

  private def getScaledNode(pos: Position): Position =
    if (isLarger(pos.scale)) {
      d(s"Scale:Larger: $pos")
      val parent = getParent(pos.node)
      val x = pos.x / gridSize + pos.node.x
      val y = pos.y / gridSize + pos.node.y
      val scale = pos.scale / gridSize
      getScaledNode(Position(parent, x, y, scale))
    } else if (isSmaller(pos.scale)) {
      d(s"Scale:Smaller: $pos")
      val childX = floor(pos.x % gridSize).toInt
      val childY = floor(pos.y % gridSize).toInt
      val child = getChild(pos.node, childX, childY)
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

  private def sign(a: Double): Int = if (a >= 0) 1 else -1

  // Creating new nodes

  private def getParent(from: Node): Node = from.parent match {
    case Some(node) => node
    case None => {
      val parent = from.createParent()
      d(s"getParent: ${_root} -> $parent")
      _root = parent
      parent
    }
  }

  private def getChild(from: Node, x: Int, y: Int): Node =
    from.getChild(x, y) match {
      case Some(node) => node
      case None => {
        val newChild = from.addChild(x, y)
        d(s"getChild: $newChild")
        newChild
      }
    }

  // Utilities

  private case class Position(node: Node, x: Double, y: Double, scale: Double)

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
}