package com.auginte.zooming

/**
 * Hierarchy element.
 * Used for relative-absolute coordinates conversion.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Node(val x: Int, val y: Int) extends Iterable[Node] {
  private[Node] val id = NodeDebug.getId()

  private var _parent: Option[Node] = None

  private var _children: List[Node] = List[Node]()

  def createParent(): Node = {
    val parentNode: Node = new Node(0, 0)
    _parent = Some(parentNode)
    parentNode.addChild(this)
    parentNode
  }

  def addChild(x: Int, y: Int): Node = getChild(x, y) match {
    case Some(child) => child
    case None => {
      val child = new Node(x, y)
      child._parent = Some(this)
      addChild(child)
      child
    }
  }

  def getChild(x: Int, y: Int): Option[Node] =
    children find (node => node.x == x && node.y == y)

  private def addChild(child: Node): Unit = {
    _children ::= child
  }

  def parent: Option[Node] = _parent

  def children: Seq[Node] = _children

  override def iterator: Iterator[Node] = _children.iterator

  override def toString: String = {
    val parentId = if (parent.isDefined) parent.get.id else "ø"
    s"[{$id}: ${x}x${y} of $parentId]"
  }

  def isChildOf(distantParent: Node): Boolean = {
    def isChild(child: Node, end: Boolean): Boolean = {
      if (end) return child eq distantParent
      child.parent match {
        case Some(parent) if (parent eq distantParent) => isChild(parent, true)
        case Some(parent) => isChild(parent, false)
        case None => isChild(child, true)
      }
    }
    if (this eq distantParent) false else isChild(this, false)
  }

  def hierarchyAsString(indent: Int = 0): String = children.foldLeft {
    def repeat(part: String, times: Int): String = {
      val buffer = new StringBuilder
      for (i <- 1 to times) buffer ++= part
      buffer.toString()
    }

    repeat(" ", indent) + toString + "\n"
  } {
    (sum: String, child: Node) => sum + child.hierarchyAsString(indent + 1)
  }

}

/**
 * Companion object for [[com.auginte.zooming.Node]]
 */
object Node {
  def apply(x: Int, y: Int) = new Node(x, y)

  def unapply(node: Node): Option[(Option[Node], Seq[Node])] =
    Some(node.parent, node.children)

}

//FIXME: debug
object NodeDebug {
  private var i = 0;

  def getId() = {
    i += 1
    i
  }
}