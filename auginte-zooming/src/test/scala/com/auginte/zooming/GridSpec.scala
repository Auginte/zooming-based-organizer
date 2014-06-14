package com.auginte.zooming

import com.auginte.test.UnitSpec

/**
 * Testing [[com.auginte.zooming.Grid]]
 *
 * Testing with ScaleFactor = 100
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class GridSpec extends UnitSpec {

  "Grid" when {
    "exporting hierarchy" should {
      "provide root element" in {
        val root: Node = standardGrid().root
      }
      "iterate hierarchy from root" in {
        val s = standardGrid()
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
      "provide textual and numeric representation of coordinates" in {
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
        assertResult(("123456", "90001", "10000")) {
          standardGrid().absoluteTextual(r1, r3)
        }
        assertResult(("1234", "900", "100")) {
          standardGrid().absoluteTextual(r2, r3)
        }
        assertResult(("3456", "1", "100")) {
          standardGrid().absoluteTextual(r1, r2)
        }
        assertResult(("0", "0", "1")) {
          standardGrid().absoluteTextual(r1, r1)
        }
        assertResult(Distance(123456, 90001, 10000)) {
          standardGrid().absoluteChildParent(r1, r3)
        }
        assertResult(Distance(1234, 900, 100)) {
          standardGrid().absoluteChildParent(r2, r3)
        }
        assertResult(Distance(3456, 1, 100)) {
          standardGrid().absoluteChildParent(r1, r2)
        }
        assertResult(Distance(0, 0, 1)) {
          standardGrid().absoluteChildParent(r1, r1)
        }
        intercept[IllegalArgumentException] {
          standardGrid().absoluteTextual(r3, r1)
        }
        intercept[IllegalArgumentException] {
          standardGrid().absoluteChildParent(r3, r1)
        }
      }
    }
    "suggesting node from absolute coordinates" should {
      "use root, when distance is small" in {
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 0, 0, 1)
        val n2 = grid.getNode(root, 99, 0, 1)
        val n3 = grid.getNode(root, 99, 99, 1)
        val n4 = grid.getNode(root, -99, 0, 1)
        val n5 = grid.getNode(root, -99, -99, 1)
        val n6 = grid.getNode(root, -99, -99, 99)
        val n7 = grid.getNode(root, -99, -99, 1 / 99.0)
        assert(n1 === root)
        assert(n2 === root)
        assert(n3 === root)
        assert(n4 === root)
        assert(n5 === root)
        assert(n6 === root)
        assert(n7 === root)
        val n8 = grid.getNode(root, 100, 0, 1)
        val n9 = grid.getNode(root, 0, -101, 1)
        val n10 = grid.getNode(root, 0, -101, 101)
        val n11 = grid.getNode(root, 0, -101, 1 / 101.0)
        assert(n8 !== root)
        assert(n9 !== root)
        assert(n10 !== root)
        assert(n11 !== root)
      }
      "create child for smaller" in {
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 0, 0, 1 / 100.0)
        val n2 = grid.getNode(root, 1, 2, 1 / 102.0)
        val n3 = grid.getNode(n2, 1, 2, 1 / 199.9)
        val n4 = grid.getNode(n3, -10, -3, 1 / 123.4)
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
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 0, 0, 100.0)
        val n2 = grid.getNode(root, 1, 2, 102.0)
        val n3 = grid.getNode(n2, 1, 2, 199.9)
        val n4 = grid.getNode(n3, -10, -3, 123.4)
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
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 100, 0, 1)
        val n2 = grid.getNode(root, 0, 123, 1)
        val n3 = grid.getNode(root, 499, 999, 1)
        val r2 = grid.root
        val n4 = grid.getNode(root, 14599, 999, 1)
        val r1 = grid.root
        val r3 = n4.parent.getOrElse(invalid)
        val n5 = grid.getNode(n4, 123, 999, 1)
        val n6 = grid.getNode(n4, 223, 999, 1)
        val n7 = grid.getNode(n4, 299, 900, 1)
        val n8 = grid.getNode(root, 19999, 12345, 1)
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
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, -100, 0, 1)
        val n2 = grid.getNode(root, 0, -123, 1)
        val n3 = grid.getNode(root, -499, -999, 1)
        val r2 = grid.root
        val n4 = grid.getNode(root, -14599, -999, 1)
        val r1 = grid.root
        val r3 = n4.parent.getOrElse(invalid)
        val n5 = grid.getNode(n4, -123, -999, 1)
        val n6 = grid.getNode(n4, -223, -999, 1)
        val n7 = grid.getNode(n4, -299, -900, 1)
        val n8 = grid.getNode(root, -19999, -12345, 1)
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
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 111, 222, 1)
        val n2 = grid.getNode(root, -333, 444, 1)
        val n3 = grid.getNode(root, -555, -666, 1)
        val n4 = grid.getNode(root, 777, -888, 1)
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
        val n5 = grid.getNode(n4, 123, 999, 1)
        val n6 = grid.getNode(n1, -234, 333, 1)
        val n7 = grid.getNode(n2, -111, -888, 1)
        val n8 = grid.getNode(n3, 666, -111, 1)
        val n9 = grid.getNode(n4, -111, 222, 1)
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
        val (root, grid) = rootGridPair()
        //          r1________
        //          |         \
        //      ____r2_____   r5
        //     /    |      \   \
        //   r3   root_    r4   r6
        //   /   /  |  \    \    \
        //  n5  n1  n2  n3  n4   n6
        //   / \
        // n7  n8
        val n1 = grid.getNode(root, -123 / 100.0, 0, 1 / 100.0)
        val n2 = grid.getNode(root, 99 / 100.0, 234 / 100.0, 1 / 112.0)
        val n3 = grid.getNode(root, 9967 / 100.0, -9934 / 100.0, 1 / 134.0)
        val n4 = grid.getNode(root, 12345 / 100.0, 0, 1 / 100.0)
        val n5 = grid.getNode(root, 123 / 100.0, -987654 / 100.0, 1 / 101.0)
        val n6 = grid.getNode(root, 0, 98765432 / 100.0, 1 / 101.0)
        val n7 = grid.getNode(n2, 1234567 / 100.0, -23456 / 100.0, 1 / 101.0)
        val n8 = grid.getNode(n4, 9999 / 100.0, 0, 1 / 101.0)
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
        val (root, grid) = rootGridPair()
        //   _____ r4_______
        //  /      |        \
        // n6  ____r3____    n5
        //    /    |     \
        //   n4  __r2__  n3
        //      /  |   \
        //     n2  r1  n1
        //         |
        //         r
        val n1 = grid.getNode(root, 10001, 10001, 100)
        val n2 = grid.getNode(root, -123456, -987654, 101)
        val n3 = grid.getNode(root, 12345678, -87654321, 12345)
        val n4 = grid.getNode(root, -123456, 12345678, 12345)
        val n5 = grid.getNode(root, 1234567890, 0, 1234567)
        val r1 = root.parent.getOrElse(invalid)
        val r2 = r1.parent.getOrElse(invalid)
        val r3 = r2.parent.getOrElse(invalid)
        val r4 = r3.parent.getOrElse(invalid)
        val n6 = grid.getNode(r1, -12345678, 123456, 10000)
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
        val (root, grid) = rootGridPair()
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
        val r1 = grid.getNode(root, 1234, 1, 100)
        val n1 = grid.getNode(root, 1234567890, 87654321, 100)
        val n11 = grid.getNode(root, 1234567890, 87654321, 1)
        val n111 = grid.getNode(root, 1234567890, 87654321, 1 / 100.0)
        val n1111 = grid.getNode(root, 1234567890, 87654321, 1 / 10000.0)
        val n2 = grid.getNode(root, 1299887766, 87654322, 100)
        val n3 = grid.getNode(root, 1434000000, 1221000000, 10000)
        val n4 = grid.getNode(n1, 2230000, 12120000, 100)
        val n5 = grid.getNode(n2, 1000000, 1000000, 10000)
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
        val (oldRoot, grid) = rootGridPair()
        val deep = 123 // 12345 in ~11 s.
        for (i <- 1 to deep) {
          grid.getNode(grid.root, 0, 0, 100)
        }
        val top = grid.root
        var parent = grid.root
        for (i <- 1 to deep) {
          parent = grid.getNode(parent, 0, 0, 1 / 100.0)
        }
        assert(parent === oldRoot)
        assert(top === grid.root)
        val scale = "1" + ("00" * deep)
        assertResult(("0", "0", scale)) {
          grid.absoluteTextual(parent, top)
        }
      }
      "create multilevel hierarchy for large translations" in {
        val (oldRoot, grid) = rootGridPair()
        //  root
        //  :
        //  |______            r2         (0,0)
        //  |\____ \____       r1         (0,0)          (1, 0)     (99, 49)
        //  | ....  ..... .... translated (0,0) (50, 25) (0, 50) .. (50, 75)
        //
        val deep = 100 * 2 - 1 // 100*100*2-1 in ~4 s. (99,49)<-(99,99)<-(50,75)
        var translated = oldRoot
        for (i <- 1 to deep) {
          translated = grid.getNode(translated, 5000, 2500, 1)
        }
        val r1 = translated.parent.getOrElse(invalid)
        val r2 = r1.parent.getOrElse(invalid)
        assert(r2 === grid.root)
        assertResult(("9950", "4975", "10000")) {
          grid.absoluteTextual(translated, grid.root)
        }
      }
    }
    "comparing absolute position between nodes" should {
      "return zero for same nodes" in {
        val (root, grid) = rootGridPair()
        val absolute = grid.absoluteBetweenFirst(root, root)
        assert(Distance(0, 0, 1) === absolute)
      }
      "return scale value for straight child to parent relation" in {
        //   R  ^
        //   :  |
        //   C  |
        val (root, grid) = rootGridPair()
        val child1 = grid.getNode(root, 0, 0, 0.01)
        val child2 = grid.getNode(child1, 0, 0, 0.01)
        val child3 = grid.getNode(child2, 0, 0, 0.01)
        assert(child1 isChildOf root)
        assert(child2 isChildOf child1)
        assert(child3 isChildOf child2)
        assert(Distance(0, 0, 100) === grid.absoluteBetweenFirst(child1, root))
        assert(Distance(0, 0, 10000) === grid.absoluteBetweenFirst(child2, root))
        assert(Distance(0, 0, 1000000) === grid.absoluteBetweenFirst(child3, root))
        assert(Distance(0, 0, 10000) === grid.absoluteBetweenFirst(child3, child1))
      }
      "return scale value for straight parent to child relation" in {
        //   R  |
        //   :  |
        //   C  v
        val (root, grid) = rootGridPair()
        val child1 = grid.getNode(root, 0, 0, 0.01)
        val child2 = grid.getNode(child1, 0, 0, 0.01)
        val child3 = grid.getNode(child2, 0, 0, 0.01)
        assert(child1 isChildOf root)
        assert(child2 isChildOf child1)
        assert(child3 isChildOf child2)
        assert(Distance(0, 0, 0.01) === grid.absoluteBetweenFirst(root, child1))
        assert(Distance(0, 0, 0.0001) === grid.absoluteBetweenFirst(root, child2))
        assert(Distance(0, 0, 0.000001) === grid.absoluteBetweenFirst(root, child3))
        assert(Distance(0, 0, 0.0001) === grid.absoluteBetweenFirst(child1, child3))
      }
      "return translation value for same scale level relation" in {
        //   .             r2____________
        //   |\            |             \
        //   | \           r1_________    \
        //   |  \          |     \    \    \
        //   A-->B         root   n1   n2   n3
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 100, 0, 1)
        val n2 = grid.getNode(root, -9900, 9900, 1)
        val n3 = grid.getNode(root, 123456, -654321, 1)
        val r1 = root.parent.getOrElse(invalid)
        val r2 = r1.parent.getOrElse(invalid)
        assert(root isChildOf r1)
        assert(r1 isChildOf r2)
        assert(n1 isChildOf r1)
        assert(n2 isChildOf r1)
        assert(n3 isChildOf r2)
        assert(Distance(100, 0, 1) === grid.absoluteBetweenFirst(root, n1))
        assert(Distance(-9900, 9900, 1) === grid.absoluteBetweenFirst(root, n2))
        assert(Distance(123400, -654300, 1) === grid.absoluteBetweenFirst(root, n3))
        assert(Distance(-100, 0, 1) === grid.absoluteBetweenFirst(n1, root))
        assert(Distance(9900, -9900, 1) === grid.absoluteBetweenFirst(n2, root))
        assert(Distance(-123400, 654300, 1) === grid.absoluteBetweenFirst(n3, root))
        assert(Distance(-10000, 9900, 1) === grid.absoluteBetweenFirst(n1, n2))
        assert(Distance(10000, -9900, 1) === grid.absoluteBetweenFirst(n2, n1))
        assert(Distance(-123300, 654300, 1) === grid.absoluteBetweenFirst(n3, n1))
        assert(Distance(133300, -664200, 1) === grid.absoluteBetweenFirst(n2, n3))
      }
      "return scale and translation value for child to uncle relation" in {
        //   .__             r3____________
        //   |  \            |             \
        //   |   \           r2_________    n3
        //   |  .>B          |     \    \
        //   A_/             r1    n1   n2
        //                   |
        //                   root
        val (root, grid) = rootGridPair()
        val r1 = grid.getNode(root, 0, 0, 100)
        val n1 = grid.getNode(root, 120000, -340000, 100)
        val n2 = grid.getNode(root, -990000, 990000, 100)
        val n3 = grid.getNode(root, 12340000, 43210000, 10000)
        val r2 = r1.parent.getOrElse(invalid)
        val r3 = r2.parent.getOrElse(invalid)
        assertParents(root, r1, r2, r3)
        assertParents(n1, r2, r3)
        assertParents(n2, r2, r3)
        assertParents(n3, r3)

        assert(Distance(0, 0, 100) === grid.absoluteBetweenFirst(root, r1))
        assert(Distance(120000, -340000, 100) === grid.absoluteBetweenFirst(root, n1))
        assert(Distance(-990000, 990000, 100) === grid.absoluteBetweenFirst(root, n2))
        assert(Distance(12000000, 43000000, 10000) === grid.absoluteBetweenFirst(root, n3))
      }
      "return scale and translation value for child to grandchild relation" in {
        //   .__            r0____
        //   |  \           |     \
        //   |   \          root   r1__r2
        //   A_   \                 \   \
        //     `-->B                 n1  n2
        //                            \
        //                             n3
        val (root, grid) = rootGridPair()
        val n1 = grid.getNode(root, 1234, -5678, 0.01)
        val n2 = grid.getNode(root, 9876, -9876, 0.01)
        val n3 = grid.getNode(root, 1234.12, -5678.34, 0.0001)
        val r1 = n1.parent.getOrElse(invalid)
        val r2 = n2.parent.getOrElse(invalid)
        val r0 = r1.parent.getOrElse(invalid)
        assertParents(root, r0)
        assertParents(n3, n1, r1, r0)
        assertParents(n2, r2, r0)
        assert(Distance(1234, -5678, 0.01) === grid.absoluteBetweenFirst(root, n1))
        assert(Distance(9876, -9876, 0.01) === grid.absoluteBetweenFirst(root, n2))
        assert(Distance(1234, -5678, 0.0001) === grid.absoluteBetweenFirst(root, n3))
      }
    }
    "optimising active camera in infinity zooming" should {
      "invariant gui, but optimise camera absolute, when camera translated" in {
        val (camera, grid) = rootGridPair()
        val (newCamera, translation) = (grid.getCameraNode(camera, 100, 0, 1), Distance(106.0, -1.0, 1.0))
        assertXY(newCamera, -1, 0)
        val (n1, a1, g1) = (grid.getNode(camera, 0, 0, 1), Distance(83.0, 85.0, 1.0), Distance(189.0, 84.0, 1.0))
        val (n2, a2, g2) = (grid.getNode(camera, 200, 0, 1), Distance(18.0, 88.0, 1.0), Distance(324.0, 87.0, 1.0))
        val (n3, a3, g3) = (grid.getNode(camera, 0, 200, 1), Distance(89.0, 13.0, 1.0), Distance(195.0, 212.0, 1.0))
        val (n4, a4, g4) = (grid.getNode(camera, 200, 200, 1), Distance(38.0, 14.0, 1.0), Distance(344.0, 213.0, 1.0))
        assertXY(n1, 0, 0)
        assertXY(n2, 2, 0)
        assertXY(n3, 0, 2)
        assertXY(n4, 2, 2)
        val newTranslation = grid.absoluteCamera(camera, newCamera, translation)
        assert(Distance(6.0, -1.0, 1.0) === newTranslation)
        assert(g1 === grid.absolute(camera, translation, n1, a1))
        assert(g2 === grid.absolute(camera, translation, n2, a2))
        assert(g3 === grid.absolute(camera, translation, n3, a3))
        assert(g4 === grid.absolute(camera, translation, n4, a4))
        assert(g1 === grid.absolute(newCamera, newTranslation, n1, a1))
        assert(g2 === grid.absolute(newCamera, newTranslation, n2, a2))
        assert(g3 === grid.absolute(newCamera, newTranslation, n3, a3))
        assert(g4 === grid.absolute(newCamera, newTranslation, n4, a4))
      }
      "invariant gui, but optimise camera absolute, when camera zoomed" in {
        val (camera, grid) = rootGridPair()
        val n1 = grid.getNode(camera, 100, 100, 1)
        val a1 = Distance(2.0,6.0,1.0)
        val g1 = Distance(18534.787497310354, 19724.615300846748, 101.17921587238027)
        assertXY(n1, 1, 1)
        val translation = Distance(81.18769657879855,88.94730346325152,101.17921587238027)
        val newCamera = grid.getCameraNode(camera, translation.x, translation.y, translation.scale)
        assertXY(camera, 0, 0)
        assertXY(newCamera, 0, 0)
        assert(camera isChildOf newCamera)
        val newTranslation = grid.absoluteCamera(camera, newCamera, translation)
        assert(Distance(81.18769657879855,88.94730346325152,1.0117921587238028) === newTranslation)
        val precision = Some(1E-7)
        assertDistance(g1, grid.absolute(camera, translation, n1, a1), precision)
        assertDistance(g1, grid.absolute(newCamera, newTranslation, n1, a1), precision)
      }
    }
    "calculating absolute position in infinity zooming" should {
      "optimise absolute coordinates, when no camera transformation" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(0, 0, 1)
        val absolute = grid.absoluteNew(cameraAbsolute, 123, 78)
        assert(Distance(123, 78, 1) === absolute)
        val node = grid.getNode(camera, absolute)
        val root = camera.parent.getOrElse(invalid)
        assertParents(camera, root)
        assertParents(node, root)
        assertXY(node, 1, 0)
        val optimised = grid.absoluteNew(camera, node, absolute)
        assert(Distance(23, 78, 1) === optimised)
      }
      "optimise absolute coordinates, when no zooming" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(12, 34, 1)
        val absolute = grid.absoluteNew(cameraAbsolute, 123 + 12, 78 + 34)
        assert(Distance(123, 78, 1) === absolute)
        val node = grid.getNode(camera, absolute)
        val root = camera.parent.getOrElse(invalid)
        assertParents(camera, root)
        assertParents(node, root)
        assertXY(node, 1, 0)
        val optimised = grid.absoluteNew(camera, node, absolute)
        assert(Distance(23, 78, 1) === optimised)
      }
      "optimise absolute coordinates, when simple zooming" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(0, 0, 0.5)
        val absolute = grid.absoluteNew(cameraAbsolute, 123, 78)
        assert(Distance(246, 156, 2) === absolute)
        val node = grid.getNode(camera, absolute)
        val root = camera.parent.getOrElse(invalid)
        assertParents(camera, root)
        assertParents(node, root)
        assertXY(node, 2, 1)
        val optimised = grid.absoluteNew(camera, node, absolute)
        assert(Distance(46, 56, 2) === optimised)
      }
      "simple sum only absolute, when on same node and same scale" in {
        val (root, grid) = rootGridPair()
        val camera = Distance(12, 34, 1)
        val element1 = Distance(56, 78, 1)
        val element2 = Distance(90, -34, 1)
        val gui1 = grid.absolute(root, camera, root, element1)
        assertDistance(Distance(12 + 56, 34 + 78, 1), gui1)
        val gui2 = grid.absolute(root, camera, root, element2)
        assertDistance(Distance(12 + 90, 34 - 34, 1), gui2)
      }
      "sum only absolute, when on same node with zooming" in {
        val (root, grid) = rootGridPair()
        val camera = Distance(12, 34, 0.25)
        val element1 = Distance(56, 78, 1.5)
        val element2 = Distance(90, -34, 0.5)
        val gui1 = grid.absolute(root, camera, root, element1)
        assertDistance(Distance((12 + 56) * 0.25, (34 + 78) * 0.25, 0.25 * 1.5), gui1)
        val gui2 = grid.absolute(root, camera, root, element2)
        assertDistance(Distance((12 + 90) * 0.25, (34 - 34) * 0.25, 0.25 * 0.5), gui2)
      }
      "calculate gui absolute, when camera and elements in different nodes" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(0, 0, 1)
        val elementAbsolute = Distance(123, 456, 1)
        val element = grid.getNode(camera, elementAbsolute)
        assertXY(element, 1, 4)
        val optimised = grid.absoluteNew(camera, element, elementAbsolute)
        assert(Distance(23, 56, 1) === optimised)
        val absolute = grid.absolute(camera, cameraAbsolute, element, optimised)
        assert(Distance(123, 456, 1) === absolute)
      }
      "calculate gui absolute, when moved camera and elements in different nodes" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(-10, -20, 1)
        val elementAbsolute = grid.absoluteNew(cameraAbsolute, 113, 436)
        assert(Distance(123, 456, 1) === elementAbsolute)
        val element = grid.getNode(camera, elementAbsolute)
        assertXY(element, 1, 4)
        val optimised = grid.absoluteNew(camera, element, elementAbsolute)
        assert(Distance(23, 56, 1) === optimised)
        val absolute = grid.absolute(camera, cameraAbsolute, element, optimised)
        assert(Distance(113, 436, 1) === absolute)
      }
      "calculate gui absolute, when simple zoomed camera and elements in different nodes" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(0, 0, 0.5)
        val elementAbsolute = grid.absoluteNew(cameraAbsolute, 123, 456)
        assert(Distance(246, 912, 2) === elementAbsolute)
        val element = grid.getNode(camera, elementAbsolute)
        assertXY(element, 2, 9)
        val optimised = grid.absoluteNew(camera, element, elementAbsolute)
        assert(Distance(46, 12, 2) === optimised)
        val absolute = grid.absolute(camera, cameraAbsolute, element, optimised)
        assert(Distance(123, 456, 1) === absolute)
      }
    }
  }


  //
  // Helpers
  //

  def standardGrid() = new Grid() {
    override val gridSize = 100
  }

  def rootGridPair() = {
    val grid = standardGrid()
    (grid.root, grid)
  }

  def assertXY(node: Node, x: Int, y: Int): Unit =
    assert(node.x == x && node.y == y,
      s"Expeced ${x}x${y}, but actual ${node.x}x${node.y} in ${node}\n")

  def assertWithTolerance(expected: Double, actual: Double, tolerance: Double): Unit =
    assert(expected > actual - tolerance && expected < actual + tolerance,
      s"Expeced ${expected} +- ${tolerance}, but actual ${actual}\n")

  def assertDistance(expected: Distance, actual: Distance, tolerance: Option[Double] = None): Unit = tolerance match {
    case Some(t) => assert(expected.x > actual.x - t && expected.x < actual.x + t &&
      expected.y > actual.y - t && expected.y < actual.y + t &&
      expected.scale > actual.scale - t && expected.scale < actual.scale + t,
      s"Expeced ${expected} +- ${t}, but actual ${actual}\n")
    case None => assert(expected === actual, s"Expeced ${expected}, but actual ${actual}\n")
  }


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