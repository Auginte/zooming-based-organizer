package com.auginte.zooming

import scala.annotation.tailrec

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
  NodeDebug.resetId()
  private var _root = new Node(0, 0)

  /**
   * Distance between nodes.
   */
  val gridSize: Int = 100

  /**
   * When calculating new absolute position, approximation used.
   */
  val absolutePrecision = 1000000

  var debug_distances: StringBuilder = new StringBuilder()

  var debug_distances2: StringBuilder = new StringBuilder()

  var debug_distances3: StringBuilder = new StringBuilder()

  var debug_distances4: StringBuilder = new StringBuilder()

  private lazy val scaleLog10 = Math.log10(gridSize)

  //
  // High level absolute coordinate (Node -> absolute) functions
  //

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
   * Includes upper node coordinates. For same nodes, returns upper node coordinates.
   *
   * @throws IllegalArgumentException When there are no child-parent relation
   * @see [[absoluteChildParent]]
   */
  def absoluteTextual(child: Node, parent: Node): TextualCoordinates =
    if (child == parent) (parent.x + "", parent.y + "", "1")
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
   *
   * Includes upper node coordinates. For same nodes, returns upper node coordinates.
   *
   * @throws IllegalArgumentException When there are no child-parent relation
   * @see [[absoluteTextual]]
   */
  private[zooming] def absoluteChildParent(child: Node, parent: Node): Distance =
    if (child == parent) Distance(parent.x, parent.y, 1)
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
   * Absolute coordinates from parent perspective.
   *
   * @see absoluteChild
   * @deprecated
   */
  private def absoluteParent(fromChild: Distance): Distance =
    Distance(fromChild.x / fromChild.scale, fromChild.y / fromChild.scale, 1)

  /**
   * Absolute coordinates for node from Graphical user interface perspective (including camera and element's position).
   * Assuming GUI coordinates are in same scale/hierarchy level as camera.
   *
   * Transformations:
   * {{{
   *   G_p = (E_p - C_p) / C_s
   *   G_s = E_s / C_s
   *   // G_p - Graphical user interface (GUI, e.g. JavaFx) position
   *   // C_p - Camera's absolute position (treating camera as node in grid, not as translation in GUI)
   *   // E_p - Element's own absolute position
   *   // C_s - Camera's absolute zooming (multiplicative inverse of scale)
   *   // E_s - Element's absolute scale
   *   // G_s - GUI scale of element (as it appears to user)
   * }}}
   *
   * @see [[Distance.asCameraNode]]
   */
  def absolute(camera: Node, cameraPosition: Distance, element: Node, elementPosition: Distance): Distance =
    if (camera == element) {
      val cp = cameraPosition
      val ep = elementPosition

      d(s"cameraPosition=$cameraPosition")
      d(s"elementPosition=$elementPosition")
      d(s"node=$element")
      d(s" G = ${ep.x - cp.x} / ${cp.scale} x ${ep.y - cp.y} / ${cp.scale}")
      Distance((ep.x - cp.x) / cp.scale, (ep.y - cp.y) / cp.scale, ep.scale / cp.scale)
    } else {
      d(s"IN $camera $cameraPosition --> $element $elementPosition")
      //      val fromElement = absoluteNew(camera, element, cameraPosition)
      //      d(s"fromElement=$fromElement")

      d(s"---------------------")
      val betweenNodes = absoluteBetweenFirst(camera, element)
      d(s"betweenNodes=$betweenNodes")
      val elementAtCameraLevel = elementPosition * betweenNodes.scale
      d(s"elementAtCameraLevel=$elementAtCameraLevel")
      val elementFromCamera = (betweenNodes + elementAtCameraLevel) withScale elementAtCameraLevel.scale

      absolute(camera, cameraPosition, camera, elementFromCamera)

      //      d(s"_____________________")
      //      absolute(element, fromElement, element, elementPosition)
    }

  /**
   * Generating new absolute position for new node, using GUI coordinates
   * @see [[Distance.asCameraNode]]
   */
  def absoluteNew(cameraPosition: Distance, x: Double, y: Double): Distance = {
    val p = cameraPosition
    Distance((x * p.scale) + p.x, (y * p.scale) + p.y, p.scale)
  }

  /**
   * Generating new absolute position from first node (e.g. camera's) perspective
   */
  def absoluteFirst(from: Node, to: Node, absoluteFrom: Distance): Distance = {
    val between = absoluteBetweenFirst(from, to)
    (absoluteFrom -- between) zoomed absoluteFrom.scale
  }

  /**
   * Generating new absolute position from last node (e.g. element's) perspective
   */
  def absoluteNew(from: Node, to: Node, absoluteFirst: Distance): Distance = {
    val between = absoluteBetweenFirst(from, to)
    d(s"NEW IN $from -> $to | $absoluteFirst")
    val absoluteAtFirst = absoluteFirst / between.scale
    d(s"NEW between=$between")
    d(s"NEW sub1=${between - absoluteAtFirst}")
    d(s"NEW sub1=${between - absoluteFirst}")
    val rez = (between - absoluteFirst) * -1 withScale (between.scale * absoluteFirst.scale)
    rez
  }

  /**
   * Calculating new location, if camera node should be optimised.
   *
   * @return absolute from second node perspective
   * @see [[getCameraNode]]
   */
  def absoluteCamera(from: Node, to: Node, absoluteFrom: Distance): Distance = {
    d(s"CAM: INPUT $from -> $to | $absoluteFrom")
    val between = absoluteBetweenFirst(from, to)
    d(s"CAM: BETWEEN $between")
    d(s"CAM: SUM ${absoluteFrom + between}")
    d(s"CAM: DIV ${absoluteFrom.scale / between.scale}")
    val atSameLevel = (absoluteFrom - between) withScale absoluteFrom.scale
    d(s"CAM sameLevel=$atSameLevel")
    val atNewCameraLevel = atSameLevel / between.scale
    atNewCameraLevel
  }

  /**
   * Absolute position between nodes from first node perspective.
   */
  private[zooming] def absoluteBetweenFirst(from: Node, to: Node): Distance = {
    d(s"BTW INPUT#absoluteBetweenFirst#: $from -> $to")
    val parent = getCommonParent(from, to)
    d(s"BTW Parent=$parent")
    val absoluteFrom = absoluteChildParent(from, parent)
    d(s"BTW absoluteFrom=$absoluteFrom")
    val absoluteTo = absoluteChildParent(to, parent)
    d(s"BTW absoluteTo=$absoluteTo")
    val differenceAtParent = (absoluteTo -- absoluteFrom) withScale 1
    d(s"BTW differenceAtParent=$differenceAtParent")
    val factor = absoluteFrom.scale * gridSize
    (differenceAtParent * factor) withScale (absoluteFrom.scale / absoluteTo.scale)
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
  // High level best node search (absolute -> Node) functions
  //

  def root: Node = _root

  def getCameraNode(camera: Node, absolute: Distance): Node =
    getCameraNode(camera, absolute.x, absolute.y, absolute.scale)

  /**
   * Optimised node for camera
   */
  def getCameraNode(camera: Node, x: Double, y: Double, scale: Double): Node = {
    d(s"Get camera node IN: $camera ($x, $y, $scale)")
    getNode(camera, x, y, scale)
  }

  /**
   * Delegating to [[getNode]]
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

  private def shallowExists(node: Node, x: Int, y: Int): Boolean = node.parent match {
    case Some(p) if x >= 0 && y >= 0 && x < gridSize && y < gridSize => p.getChild(x, y).isDefined
    case Some(p) => true
    case None => false
  }

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
    type Sign = (Double, Double)
    def getSign(node: Node): Sign = if (node.x == 0 && node.y == 0) node.parent match {
      case Some(p) => getSign(p)
      case None => (node.x, node.y)
    } else (node.x, node.y)

    def diveUp(parent: Node, x: Double, y: Double, scale: Double, sign: Sign): Position = {
      @inline def sameSign(x: Double, y: Double, sign: Sign, parent: Node): Boolean =
        (x * sign._1 >= 0 && y * sign._2 >= 0) || (parent eq root)
      d(s"DiveUp $parent | ${x}x${y} s {$scale}")
      if (x.abs >= gridSize || y.abs >= gridSize || !sameSign(x, y, sign, parent)) {
        val _x = (x / gridSize) + parent.x
        val _y = (y / gridSize) + parent.y
        d(s"\tdiveUp ($x/$gridSize + ${parent.x}, $y/$gridSize + ${parent.y}) = ${_x}x${_y}")
        diveUp(getParent(parent), _x, _y, scale * gridSize, sign)
      } else {
        d(s"\tdived up $parent ${x}x${y} s $scale")
        Position(parent, x, y, scale)
      }
    }

    def diveDown(position: Position, distance: Double): Position = {
      @inline def round8(d: Double) = (math rint d * 1E8) / 1E8
      d(s"DiveDown $position | $distance")
      val Position(parent, x, y, scale) = position
      if (distance >= gridSize) {
        val child = getChild(parent, round8(x).toInt, round8(y).toInt)
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
    val sign = getSign(node)
    d(s"sign = $sign")
    val absolute = diveUp(node, x, y, 1, sign)
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
  private def isLarger(scale: Double) = scale != 0 && Math.log10(scale.abs) >= scaleLog10

  @inline
  private def isSmaller(scale: Double) = scale != 0 && Math.log10(scale.abs) <= -scaleLog10


  private def floor(a: Double): Double = if (a >= 0) a.floor else a.ceil

  /**
   * Filters by logical distance
   *
   * @param items graphical elements with relation to node
   * @param from node to calculate distance from
   * @param scale absolute distance in each axis
   * @param deep how deep (down) children should be checked
   * @param f closure to retrieve node by list item
   * @tparam A type of list items
   * @return filtered list
   */
  def filter[A](items: Traversable[A], from: Node, scale: Int, deep: Int, f: (A) => Node): Traversable[A] = {
    def hasParent(element: Node, parent: Node, inDistance: Int): Boolean = if (element eq parent) true
    else element.parent match {
      case Some(p) if inDistance > 0 && (p eq parent) => true
      case Some(p) if inDistance > 0 => hasParent(p, parent, inDistance - 1)
      case Some(p) if inDistance <= 0 => false
      case None => false
    }
    def getNodesAround(center: Node, distance: Int): Iterable[Node] = {
      @inline def zero(a: Int, b: Int): Boolean = a == 0 && b == 0
      for (x <- -distance to distance; y <- -distance to distance) yield getNode(center, x * gridSize, y * gridSize, 0)
    }

    val parents = getNodesAround(from, scale)

    d(s"from=$from")
    for (p <- parents) d(s"PARENT: $p")

    debug_distances = new StringBuilder(5000)
    debug_distances2 = new StringBuilder(5000)
    debug_distances3 = new StringBuilder(5000)

    val res = items.filter(element => {
      val exists = parents.exists(parent => hasParent(f(element), parent, deep))
      if (exists) {
        debug_distances.append(element.toString).append("\n")
      } else {
        debug_distances2.append(element.toString).append("\n")
      }
      exists
    })

    for (p <- parents) debug_distances3.append(p.selfAndParents.reverse).append("\n");

    d(s"all=$items")
    d(s"res=$res")
    res
  }

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
  // Transformations
  //

  def zoomCamera(cameraPosition: Distance, amount: Double, guiX: Double, guiY: Double): Distance = {
    val absoluteAmount = amount
    val zoomCenterBefore = Distance(guiX * cameraPosition.scale, guiY * cameraPosition.scale, 1)
    val zoomCenterAfter = (zoomCenterBefore * absoluteAmount) withScale 1
    (cameraPosition + zoomCenterBefore - zoomCenterAfter).zoomed(amount)
  }

  def translateCamera(transformation: Distance, diffX: Double, diffY: Double): Distance = {
    transformation.translated(diffX * transformation.scale, diffY * transformation.scale)
  }

  def translateElement(element: Node, transformation: Distance, diffX: Double, diffY: Double,
                       camera: Node, cameraPosition: Distance): Distance = {
    val scale = cameraPosition.scale / absoluteBetweenFirst(camera, element).scale
    transformation.translated(diffX * scale, diffY * scale)
  }

  def newElement(camera: Node, cameraPosition: Distance, guiX: Double, guiY: Double): (Node, Distance) = {
    val absolute = absoluteNew(cameraPosition, guiX, guiY)
    val optimisedNode = getNode(camera, absolute)
    val optimisedAbsolute = absoluteNew(camera, optimisedNode, absolute)
    (optimisedNode, optimisedAbsolute)
  }

  def validateCamera(camera: Node, transformation: Distance): (Node, Distance) = {
    val newNode = getCameraNode(camera, transformation)
    if (newNode == camera) (camera, transformation)
    else (newNode, absoluteCamera(camera, newNode, transformation))
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