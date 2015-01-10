package com.auginte.zooming

import com.auginte.common.WithId

import scala.collection.GenTraversableOnce

/**
 * Hierarchy element.
 * Used for relative-absolute coordinates conversion.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class Node(private val _x: Byte, private val  _y: Byte)
  extends WithId with Iterable[Node] with GenTraversableOnce[Node] {

  private var _parent: Option[Node] = None

  private var _children: List[Node] = List[Node]()

  def x: Byte = _x

  def y: Byte = _y

  protected[zooming] def createParent()(implicit newNode: NodeToNode = sameNode): Node = {
    val parentNode: Node = newNode(new Node(0, 0))
    _parent = Some(parentNode)
    parentNode.addChild(this)
    parentNode
  }

  protected[zooming] def addChild(x: Byte, y: Byte)(implicit newNode: NodeToNode = sameNode): Node = getChild(x, y) match {
    case Some(child) => child
    case None => 
      val child = newNode(new Node(x, y))
      child._parent = Some(this)
      addChild(child)
      child
  }

  def getChild(x: Byte, y: Byte): Option[Node] =
    children find (node => node.x == x && node.y == y)

  private def addChild(child: Node): Unit = {
    _children ::= child
  }

  def parent: Option[Node] = _parent

  def children: List[Node] = _children

  override def iterator: Iterator[Node] = _children.iterator

  def selfAndParents: List[Node] = {
    def getParents(node: Node, list: List[Node]): List[Node] = node.parent match {
      case Some(parent) => getParents(parent, parent :: list)
      case None => list
    }
    getParents(this, List(this))
  }

  override def equals(o: scala.Any): Boolean = o match {
    case n: Node if n.x == x && n.y == y && n.parent == parent && n.children == children => true
    case _ => false
  }

  override def toString(): String = {
    val parentId = if (parent.isDefined) parent.get.storageId else "ø"
    s"[$storageId: ${x}x$y of $parentId]"
  }

  def isChildOf(distantParent: Node): Boolean = {
    def isChild(child: Node, end: Boolean): Boolean = {
      if (end) return child eq distantParent
      child.parent match {
        case Some(parent) if parent eq distantParent => isChild(parent, end=true)
        case Some(parent) => isChild(parent, end=false)
        case None => isChild(child, end=true)
      }
    }
    if (this eq distantParent) false else isChild(this, end=false)
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

  def entries(): List[Node] = children.foldLeft(children)((list, element) => element.entries ::: list)
}