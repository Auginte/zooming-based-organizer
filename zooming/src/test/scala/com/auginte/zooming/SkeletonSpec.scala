package com.auginte.zooming

import com.auginte.test.UnitSpec

/**
 * Testing [[com.auginte.zooming.Skeleton]]
 *
 * Testing with ScaleFactor = 100
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class SkeletonSpec extends UnitSpec {

  "Skeleton" when {
    "exporting hierarchy" should {
      "provide root element" in {
        val root: Node = standardSkeleton().root
      }
      "iterate hierarchy from root" in {
        val s = standardSkeleton()
        val root = s.root
        val n1 = s.getNode(root, 0, 0, 201)
        val n2 = s.getNode(root, 0, 0, 0.009)
        val n21 = s.getNode(root, 10, 0, 0.009999)
        val n22 = s.getNode(root, 10, 20, 0.001)
        assert(root.parent.get === n1)
        assert(n2.parent.get === root)
        assert(n21.parent.get === root)
        assert(n22.parent.get === root)
        assert(n22.parent.get === root)
        assert(n22.parent.get !== n1)
        assert(n22.parent.get !== n21)
      }
      "provide textual representation of coordinates" in {
        // r3(12, 9) <- r2(34, 0) <- r1(56, 1)
        val r3 = new Node(12, 9)
        val r2 = r3.addChild(34, 0)
        val r1 = r2.addChild(56, 1)
        assert(r1.isChildOf(r2))
        assert(r1.isChildOf(r3))
        assert(!r1.isChildOf(r1))
        assert(!r2.isChildOf(r1))
        assert(!r3.isChildOf(r1))
        assert(!r3.isChildOf(r2))
        assertResult(("123456", "90001", "1000000")) {
          standardSkeleton().getCoordinates(r3, r1)
        }
        assertResult(("1234", "900", "10000")) {
          standardSkeleton().getCoordinates(r3, r2)
        }
        assertResult(("3456", "1", "10000")) {
          standardSkeleton().getCoordinates(r2, r1)
        }
        intercept[IllegalArgumentException] {
          standardSkeleton().getCoordinates(r1, r3)
        }
        intercept[IllegalArgumentException] {
          standardSkeleton().getCoordinates(r1, r1)
        }
      }
    }
    "suggesting node from absolute coordinates" should {
      "use root, when distance is small" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, 0, 0, 1)
        val n2 = skeleton.getNode(root, 99, 0, 1)
        val n3 = skeleton.getNode(root, 99, 99, 1)
        val n4 = skeleton.getNode(root, -99, 0, 1)
        val n5 = skeleton.getNode(root, -99, -99, 1)
        val n6 = skeleton.getNode(root, -99, -99, 99)
        val n7 = skeleton.getNode(root, -99, -99, 1 / 99.0)
        assert(n1 === root)
        assert(n2 === root)
        assert(n3 === root)
        assert(n4 === root)
        assert(n5 === root)
        assert(n6 === root)
        assert(n7 === root)
        val n8 = skeleton.getNode(root, 100, 0, 1)
        val n9 = skeleton.getNode(root, 0, -101, 1)
        val n10 = skeleton.getNode(root, 0, -101, 101)
        val n11 = skeleton.getNode(root, 0, -101, 1 / 101.0)
        assert(n8 !== root)
        assert(n9 !== root)
        assert(n10 !== root)
        assert(n11 !== root)
      }
      "create child for smaller" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, 0, 0, 1 / 100.0)
        val n2 = skeleton.getNode(root, 1, 2, 1 / 102.0)
        val n3 = skeleton.getNode(n2, 1, 2, 1 / 199.9)
        val n4 = skeleton.getNode(n3, -10, -3, 1 / 123.4)
        assert(n1.parent.get === root)
        assert(n2.parent.get === root)
        assert(n3.parent.get === n2)
        assert(n4.parent.get === n3)
        assert(n4.parent.get !== n2)
        assert(n4.parent.get !== root)
        assertXY(n1, 0, 0)
        assertXY(n2, 1, 2)
        assertXY(n3, 1, 2)
        assertXY(n4, -10, -3)
      }
      "create parent for larger" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, 0, 0, 100.0)
        val n2 = skeleton.getNode(root, 1, 2, 102.0)
        val n3 = skeleton.getNode(n2, 1, 2, 199.9)
        val n4 = skeleton.getNode(n3, -10, -3, 123.4)
        assert(root.parent.get === n1)
        assert(root.parent.get === n2)
        assert(n2.parent.get === n3)
        assert(n3.parent.get === n4)
        assert(n2.parent.get !== n4)
        assert(root.parent.get !== n4)
        assertXY(n1, 0, 0)
        assertXY(n2, 0, 0)
        assertXY(n3, 0, 0)
        assertXY(n4, 0, 0)
      }
      "create moved child for positive translated elements" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, 100, 0, 1)
        val n2 = skeleton.getNode(root, 0, 123, 1)
        val n3 = skeleton.getNode(root, 499, 999, 1)
        val r2 = skeleton.root
        val n4 = skeleton.getNode(root, 14599, 999, 1)
        val r1 = skeleton.root
        val r3 = n4.parent.getOrElse(invalid)
        val n5 = skeleton.getNode(n4, 123, 999, 1)
        val n6 = skeleton.getNode(n4, 223, 999, 1)
        val n7 = skeleton.getNode(n4, 299, 900, 1)
        val n8 = skeleton.getNode(root, 19999, 12345, 1)
        val r4 = n8.parent.getOrElse(invalid)
        //  r1______________________________
        //  |                \              \
        //  r2____________   r3_______      r4
        //  |     \   \   \   \   \   \      \
        //  root  n1  n2  n3  n4  n5  n6=n7  n8
        assert(root.parent.get === r2)
        assert(n1.parent.get === r2)
        assert(n2.parent.get === r2)
        assert(n3.parent.get === r2)
        assert(n4.parent.get === r3)
        assert(n5.parent.get === r3)
        assert(n6.parent.get === r3)
        assert(n8.parent.get === r4)
        assert(n7 === n6)
        assert(r2.parent.get === r1)
        assert(r3.parent.get === r1)
        assert(r4.parent.get === r1)
        assert(r2 !== r3)
        assert(r3 !== r4)
        assert(root !== r2)
        assert(root !== r1)
        //  r1,r2,root--n1--r3-----------+
        //  |               |            |
        //  n2          n3  |  n5 n6 n4  |
        //  +---------------r4-----------+
        //  |               |    n8      |
        //  +---------------+------------+
        assertXY(root, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r1, 0, 0)
        assertXY(r3, 1, 0)
        assertXY(r4, 1, 1)
        assertXY(n1, 1, 0)
        assertXY(n2, 0, 1)
        assertXY(n3, 4, 9)
        assertXY(n4, 45, 9)
        assertXY(n5, 46, 18)
        assertXY(n6, 47, 18)
        assertXY(n7, 47, 18)
        assertXY(n8, 99, 23)
      }
      "create moved child for negative translated elements" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, -100, 0, 1)
        val n2 = skeleton.getNode(root, 0, -123, 1)
        val n3 = skeleton.getNode(root, -499, -999, 1)
        val r2 = skeleton.root
        val n4 = skeleton.getNode(root, -14599, -999, 1)
        val r1 = skeleton.root
        val r3 = n4.parent.getOrElse(invalid)
        val n5 = skeleton.getNode(n4, -123, -999, 1)
        val n6 = skeleton.getNode(n4, -223, -999, 1)
        val n7 = skeleton.getNode(n4, -299, -900, 1)
        val n8 = skeleton.getNode(root, -19999, -12345, 1)
        val r4 = n8.parent.getOrElse(invalid)
        //      __________________________r1
        //     /               /           |
        //    r4      _______r3   ________r2
        //   /       /  / /  /   /  /  /   |
        //  n8  n7=n6 n5 n4 n9 n3 n2 n1 root
        assert(root.parent.get === r2)
        assert(n1.parent.get === r2)
        assert(n2.parent.get === r2)
        assert(n3.parent.get === r2)
        assert(n4.parent.get === r3)
        assert(n5.parent.get === r3)
        assert(n6.parent.get === r3)
        assert(n7 === n6)
        assert(n8.parent.get === r4)
        assert(r2.parent.get === r1)
        assert(r3.parent.get === r1)
        assert(r4.parent.get === r1)
        assert(r2 !== r3)
        assert(r3 !== r4)
        assert(root !== r2)
        assert(root !== r1)
        //  +----------+-----------------
        //  |  n8      |                |
        //  +----------r4---------------+
        //  |          |   n3           n2
        //  | n4 n6 n5 |                |
        //  +----------r3--n1--root-r2-r1
        assertXY(root, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r1, 0, 0)
        assertXY(r3, -1, 0)
        assertXY(r4, -1, -1)
        assertXY(n1, -1, 0)
        assertXY(n2, 0, -1)
        assertXY(n3, -4, -9)
        assertXY(n4, -45, -9)
        assertXY(n5, -46, -18)
        assertXY(n6, -47, -18)
        assertXY(n7, -47, -18)
        assertXY(n8, -99, -23)
      }
      "create moved child for translated elements with different signs" in {
        val (root, skeleton) = rootSkeletonPair()
        val n1 = skeleton.getNode(root, 111, 222, 1)
        val n2 = skeleton.getNode(root, -333, 444, 1)
        val n3 = skeleton.getNode(root, -555, -666, 1)
        val n4 = skeleton.getNode(root, 777, -888, 1)
        val r1 = n1.parent.getOrElse(invalid)
        //        n7    |    n8
        //           n3 | n4 n9
        //  ------------r------------------
        //           n2 | n1
        //        n6    |    n5
        assert(root.parent.get === r1)
        assert(n1.parent.get === r1)
        assert(n2.parent.get === r1)
        assert(n3.parent.get === r1)
        assert(n4.parent.get === r1)
        assertXY(n1, 1, 2)
        assertXY(n2, -3, 4)
        assertXY(n3, -5, -6)
        assertXY(n4, 7, -8)
        val n5 = skeleton.getNode(n4, 123, 999, 1)
        val n6 = skeleton.getNode(n1, -234, 333, 1)
        val n7 = skeleton.getNode(n2, -111, -888, 1)
        val n8 = skeleton.getNode(n3, 666, -111, 1)
        val n9 = skeleton.getNode(n4, -111, 222, 1)
        assert(n5.parent.get === r1)
        assert(n6.parent.get === r1)
        assert(n7.parent.get === r1)
        assert(n8.parent.get === r1)
        assert(n9.parent.get === r1)
        assertXY(n5, 8, 1)
        assertXY(n6, -1, 5)
        assertXY(n7, -4, -4)
        assertXY(n8, 1, -7)
        assertXY(n9, 6, -6)
      }
      "create moved child for translated smaller elements" in {
        val (root, skeleton) = rootSkeletonPair()
        //          r1________
        //          |         \
        //      ____r2_____   r5
        //     /    |      \   \
        //   r3   root_    r4   r6
        //   /   /  |  \    \    \
        //  n5  n1  n2  n3  n4   n6
        //   / \
        // n7  n8
        val n1 = skeleton.getNode(root, -123 / 100.0, 0, 1 / 100.0)
        val n2 = skeleton.getNode(root, 99 / 100.0, 234 / 100.0, 1 / 112.0)
        val n3 = skeleton.getNode(root, 9967 / 100.0, -9934 / 100.0, 1 / 134.0)
        val n4 = skeleton.getNode(root, 12345 / 100.0, 0, 1 / 100.0)
        val n5 = skeleton.getNode(root, 123 / 100.0, -987654 / 100.0, 1 / 101.0)
        val n6 = skeleton.getNode(root, 0, 98765432 / 100.0, 1 / 101.0)
        val n7 = skeleton.getNode(n2, 1234567 / 100.0, -23456 / 100.0, 1 / 101.0)
        val n8 = skeleton.getNode(n4, 9999 / 100.0, 0, 1 / 101.0)
        assert(n1.parent.get === root)
        assert(n2.parent.get === root)
        assert(n3.parent.get === root)
        val r2 = root.parent.getOrElse(invalid)
        val r3 = n5.parent.getOrElse(invalid)
        val r4 = n4.parent.getOrElse(invalid)
        val r6 = n6.parent.getOrElse(invalid)
        val r5 = r6.parent.getOrElse(invalid)
        val r1 = r2.parent.getOrElse(invalid)
        assert(r3.parent.get === r2)
        assert(r4.parent.get === r2)
        assert(r5.parent.get === r1)
        assert(n7.parent.get === n4)
        assert(n8.parent.get === n4)
        //  +------ +-------+-----------++------------+
        //  |       |       | n5        ||            |
        //  |       | n3    |           ||            |
        //  |    n1 root----+--n4:n7,n8-||       n6   |
        //  |       | n2    |           ||            |
        //  +-------+-------+-----------++------------+
        assertXY(n1, -1, 0)
        assertXY(n2, 0, 2)
        assertXY(n3, 99, -99)
        assertXY(n4, 23, 0)
        assertXY(n5, 1, -76)
        assertXY(n6, 0, 54)
        assertXY(n7, 45, -34)
        assertXY(n8, 99, 0)
        assertXY(r1, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r3, 0, -98)
        assertXY(r4, 1, 0)
        assertXY(r5, 0, 98)
        assertXY(r6, 0, 76)
      }
      "create moved child for translated larger elements" in {
        val (root, skeleton) = rootSkeletonPair()
        //   _____ r4_______
        //  /      |        \
        // n6  ____r3____    n5
        //    /    |     \
        //   n4  __r2__  n3
        //      /  |   \
        //     n2  r1  n1
        //         |
        //         r
        val n1 = skeleton.getNode(root, 10001, 10001, 100)
        val n2 = skeleton.getNode(root, -123456, -987654, 101)
        val n3 = skeleton.getNode(root, 12345678, -87654321, 12345)
        val n4 = skeleton.getNode(root, -123456, 12345678, 12345)
        val n5 = skeleton.getNode(root, 1234567890, 0, 1234567)
        val r1 = root.parent.getOrElse(invalid)
        val r2 = r1.parent.getOrElse(invalid)
        val r3 = r2.parent.getOrElse(invalid)
        val r4 = r3.parent.getOrElse(invalid)
        val n6 = skeleton.getNode(r1, -12345678, 123456, 10000)
        assert(r1 !== r2)
        assert(r2 !== r3)
        assert(r3 !== r4)
        assert(root.parent.get === r1)
        assert(r1.parent.get === r2)
        assert(r2.parent.get === r3)
        assert(r3.parent.get === r4)
        assert(n1.parent.get === r2)
        assert(n2.parent.get === r2)
        assert(n3.parent.get === r3)
        assert(n4.parent.get === r3)
        assert(n5.parent.get === r4)
        assert(n6.parent.get === r4)
        assertXY(r1, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r3, 0, 0)
        assertXY(r4, 0, 0)
        assertXY(n1, 1, 1)
        assertXY(n2, -12, -98)
        assertXY(n3, 12, -87)
        assertXY(n4, 0, 12)
        assertXY(n5, 12, 0)
        assertXY(n6, -12, 0)
      }
      "create moved parent for translated larger elements" in {
        val (root, skeleton) = rootSkeletonPair()
        //  r4_____________________
        //  |   \          \       \
        //  r3  r31____     r32__   n5
        //  |    \     \    \    \
        //  r2    r21  r22   n3   n4
        //  |      \    \
        //  r1      n1   n2
        //  |       |
        //  r       n11
        //          |
        //          n111
        //          |
        //          n1111
        val r1 = skeleton.getNode(root, 1234, 1, 100)
        val n1 = skeleton.getNode(root, 1234567890, 87654321, 100)
        val n11 = skeleton.getNode(root, 1234567890, 87654321, 1)
        val n111 = skeleton.getNode(root, 1234567890, 87654321, 1 / 100.0)
        val n1111 = skeleton.getNode(root, 1234567890, 87654321, 1 / 10000.0)
        val n2 = skeleton.getNode(root, 1299887766, 87654322, 100)
        val n3 = skeleton.getNode(root, 1434000000, 1221000000, 10000)
        val n4 = skeleton.getNode(n1, 2230000, 12120000, 100)
        val n5 = skeleton.getNode(n2, 1000000, 1000000, 10000)
        val List(r2, r3, r4) = parents(r1)
        val List(r21, r31, _) = parents(n1)
        val List(r22, _, _) = parents(n2)
        val List(r32, _) = parents(n3)
        assertParents(root, r1, r2, r3, r4)
        assertParents(n1, r21, r31, r4)
        assertParents(n11, n1, r21, r31, r4)
        assertParents(n111, n11, n1, r21, r31, r4)
        assertParents(n1111, n111, n11, n1, r21, r31, r4)
        assertParents(n2, r22, r31, r4)
        assertParents(n3, r32, r4)
        assertParents(n4, r32, r4)
        assertParents(n5, r4)
        assertXY(root, 0, 0)
        assertXY(r1, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r3, 0, 0)
        assertXY(r4, 0, 0)
        assertXY(r31, 12, 0)
        assertXY(r21, 34, 87)
        assertXY(n1, 56, 65)
        assertXY(n11, 78, 43)
        assertXY(n111, 90, 21)
        assertXY(n1111, 0, 0)
        assertXY(r22, 99, 87)
        assertXY(n2, 88, 65)
        assertXY(r32, 14, 12)
        assertXY(n3, 34, 21)
        assertXY(n4, 23 + 34, 12 + 87)
        assertXY(n5, 1 + 12, 1)
      }
      "create multilevel hierarchy for large scales" in {
        val (oldRoot, skeleton) = rootSkeletonPair()
        val deep = 123 // 12345 in ~11 s.
        for (i <- 1 to deep) {
          skeleton.getNode(skeleton.root, 0, 0, 100)
        }
        val top = skeleton.root
        var parent = skeleton.root
        for (i <- 1 to deep) {
          parent = skeleton.getNode(parent, 0, 0, 1 / 100.0)
        }
        assert(parent === oldRoot)
        assert(top === skeleton.root)
        val scale = "1" + ("00" * (deep + 1))
        assertResult(("0", "0", scale)) {
          skeleton.getCoordinates(top, parent)
        }
      }
      "create multilevel hierarchy for large translations" in (pending)
    }
    "comparing absolute distance between nodes" should {
      "give zero for same elements" in (pending)
      "give larger scale for parent->child" in (pending)
      "give lower scale for child->parent" in (pending)
      "give scale in multilevel hierarchy" in (pending)
      "give translation relative to parent node" in (pending)
      "give translation in multilevel hierarchy" in (pending)
    }
  }


  //
  // Helpers
  //

  def standardSkeleton() = new Skeleton(100)

  def rootSkeletonPair() = {
    val skeleton = standardSkeleton()
    (skeleton.root, skeleton)
  }

  def assertXY(node: Node, x: Int, y: Int): Unit =
    assert(node.x == x && node.y == y,
      s"Expeced ${x}x${y}, but actual ${node.x}x${node.y} in ${node}\n")


  def assertParents(node: Node, parents: Node*): Unit = {
    def formatError(message: String): String = {
      message + s" Expected $node with parents " + parents.mkString(" -> ")
    }

    def assertParents(node: Node, parents: Seq[Node]): Unit = {
      parents match {
        case Seq(parent) => node.parent match {
          case Some(nodeParent) => assert(nodeParent === parent,
            formatError(s"Last $nodeParent != $parent"))
          case None => fail(formatError("Not enough parents. " +
            s"Last $node !-> " + parents.mkString("->")))
        }
        case parent +: tail => node.parent match {
          case Some(nodeParent) => {
            assert(nodeParent === parent,
              formatError(s"Last $node !-> " + parents.mkString("->")))
            assertParents(nodeParent, tail)
          }
          case None => fail(formatError("Not enough parents. " +
            s"Last $node !-> " + parents.mkString("->"))
          )
        }
      }
    }

    assertParents(node, parents)
  }

  def parents(node: Node): List[Node] = {
    def parents(node: Node, xs: List[Node]): List[Node] = node.parent match {
      case Some(parent) => parents(parent, parent :: xs)
      case None => xs.reverse
    }
    parents(node, List())
  }

  object invalid extends Node(-1, -1) {
    override def toString = "Invalid node"

    override def equals(obj: scala.Any): Boolean = false
  }

}