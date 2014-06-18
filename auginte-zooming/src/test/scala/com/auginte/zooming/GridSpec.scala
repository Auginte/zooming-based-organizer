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
        assertResult(("56", "1", "1")) {
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
        assertResult(Distance(56, 1, 1)) {
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
      "invariant gui, but optimise camera absolute, when same level camera translated" in {
        val (camera, grid) = rootGridPair()
        val translation = Distance(106.0, -1.0, 1.0).asCameraNode
        val newCamera = grid.getCameraNode(camera, translation)
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
        assert(Distance(6.0, -1.0, 1.0).asCameraNode === newTranslation)
        assert(g1 === grid.absolute(camera, translation, n1, a1))
        assert(g2 === grid.absolute(camera, translation, n2, a2))
        assert(g3 === grid.absolute(camera, translation, n3, a3))
        assert(g4 === grid.absolute(camera, translation, n4, a4))
        assert(g1 === grid.absolute(newCamera, newTranslation, n1, a1))
        assert(g2 === grid.absolute(newCamera, newTranslation, n2, a2))
        assert(g3 === grid.absolute(newCamera, newTranslation, n3, a3))
        assert(g4 === grid.absolute(newCamera, newTranslation, n4, a4))
      }
      "DEBUG invariant gui, but optimise camera absolute, when camera zoomed" in {
        // c - camera's node, t - camera's absolute
        // n - element's node, a - element's absolute
        // i - element's initial absolute,  g - coordinates in GUI
        //
        //  c2___                               c2
        //  |    \           =>            =>   :
        //  c1    n1    c1        c1-->n1       '--->n1
        val (c1, grid) = rootGridPair()
        val td1 = Distance(81, 88.125, 125)
        val t1 = td1.asCameraNode
        val i1 = Distance(100, 100, 1)
        val gWithoutScale = Distance(i1.x + td1.x, i1.y + td1.y, i1.scale )
        val g1 = Distance((i1.x + td1.x) * td1.scale, (i1.y + td1.y) * td1.scale, i1.scale * td1.scale) // G_p = (E_p + C_p) * C_s
        assertDistance(g1, grid.absolute(c1, t1, c1, i1), precision) // From same point

        val n1 = grid.getNode(c1, i1)
        assertXY(n1, 1, 1)
        val a1 = grid.absoluteNew(c1, n1, i1)
        assert(Distance(0,0,1.0) === a1)
        assertDistance(g1, grid.absolute(c1, t1, n1, a1), precision) // Element in other node

        val c2 = grid.getCameraNode(c1, t1)
        assertXY(c1, 0, 0)
//        assertXY(c2, 0, 0)
//        assert(c1 isChildOf c2)
        val t2 = grid.absoluteCamera(c1, c2, t1)
//        assert(Distance(81.18769657879855, 88.94730346325152, 1.0117921587238028).asCameraNode === t2)
        val gg = grid.absolute(c2, t2, n1, a1)
        assertDistance(g1, gg, precision) // Camera in other node
      }
      "invariant gui, but optimise camera absolute, when camera zoomed" in {
        // c - camera's node, t - camera's absolute
        // n - element's node, a - element's absolute
        // i - element's initial absolute,  g - coordinates in GUI
        //
        //  c2___                               c2
        //  |    \           =>            =>   :
        //  c1    n1    c1        c1-->n1       '--->n1
        val (c1, grid) = rootGridPair()
        val t1 = Distance(81.18769657879855, 88.94730346325152, 101.17921587238027).asCameraNode
        val g1 = Distance(18534.787497310354, 19724.615300846748, 101.17921587238027)
        val i1 = Distance(102, 106, 1)
        assertDistance(g1, grid.absolute(c1, t1, c1, i1), precision) // From same point

        val n1 = grid.getNode(c1, i1)
        assertXY(n1, 1, 1)
        val a1 = grid.absoluteNew(c1, n1, i1)
        assert(Distance(2.0,6.0,1.0) === a1)
        assertDistance(g1, grid.absolute(c1, t1, n1, a1), precision) // Element in other node

        val c2 = grid.getCameraNode(c1, t1)
        assertXY(c1, 0, 0)
        assertXY(c2, -81, -88)
        assert(c2 isChildOf c1)
        val t2 = grid.absoluteCamera(c1, c2, t1)
        assert(Distance(-18.769657879855117,-94.73034632515152,0.9883452756357822) === t2)
        val gg = grid.absolute(c2, t2, n1, a1)
        assertDistance(g1, gg, precision) // Camera in other node
      }
//      "invariant gui, but optimise camera absolute, when upper level camera translated" ignore {
//        // c - camera's node, t - camera's absolute
//        // n - element's node, a - element's absolute
//        // i - element's initial absolute,  g - coordinates in GUI
//        //  .____
//        //  |    \
//        //  c2--->c2_2
//        //  |
//        //  c1,n1
//        val (c1, grid) = rootGridPair()
//        val t1 = Distance(76.55955449023968, 86.31689204151021, 101.24249243014931)
//        val i1 = Distance(105.0, 106.0, 1.0)
//        val n1 = grid.getNode(c1, i1) // [{3}: 1x1 of 2]
//        val a1 = grid.absoluteNew(c1, n1, i1)
//        val g1 = Distance(2879.3815892319835, 1992.7669084892138, 101.24249243014931)
//        assertDistance(g1, grid.absolute(c1, t1, n1, a1), precision)
//        val i2 = Distance(82.80577735327356, 89.22307713283807, 0.011175230732997038)
//        val n2 = grid.getNode(c1, i2) // [{1}: 0x0 of 2]
//        val a2 = grid.absoluteNew(c1, n2, i2)
//        val g2 = Distance(632.383170927733, 294.2294221093733, 1.1314082128906244)
//        assertDistance(g2, grid.absolute(c1, t1, n2, a2), precision)
//
//        val c2 = grid.getCameraNode(c1, t1) // [{1}: 0x0 of 2] -> [{2}: 0x0 of ø]
//        val t2 = grid.absoluteCamera(c1, c2, t1)
//        assert(c1 !== c2)
//        assert(t1 !== t2)
//        assertDistance(g1, grid.absolute(c2, t2, n1, a1), precision)
//        assertDistance(g2, grid.absolute(c2, t2, n2, a2), precision)
//
//        val g1_2 = Distance(342.58778218686695,737.0882332314784,106.36789360942561)
//        val g2_2 = Distance(-2018.164931044036,-1047.4377133963412,1.1886857536682123)
//        val t1_2 = Distance(-101.77921814034579,-99.07038798814605,1.063678936094256)
//        val c1_2 = c2
//        assertDistance(g1_2, grid.absolute(c1_2, t1_2, n1, a1), precision)
//        assertDistance(g1_2, grid.absolute(c1_2, t1_2, n1, a1), precision)
//
//        val c2_2 = grid.getCameraNode(c1_2, t1_2) // [{2}: 0x0 of 4] -> [{5}: 1x0 of 4]
//        val t2_2 = grid.absoluteCamera(c1_2, c2_2, t1_2)
//        assert(c1_2 !== c2_2)
//        assert(t1_2 !== t2_2)
////        Debug.on = true
////        println(s"${grid.root.hierarchyAsString()}")
//        val g1_2_a = grid.absolute(c2_2, t2_2, n1, a1)
////        Debug.on = false
//        val g2_2_a = grid.absolute(c2_2, t2_2, n2, a2)
//        assertDistance(g1_2, g1_2_a, precision)
//        assertDistance(g2_2, g2_2_a, precision)
//      }
    }
    "debuging invariants" should {
      "tes1" in {
        val (c1, grid) = rootGridPair()
        var g: Distance = Distance()
        var c = c1
        var t = Distance()
        var pair = (c1, Distance())
        var optimised = (c1, Distance())

        pair = grid.newElement(c, t, 214.0, 188.0)
        val (n1, a1) = pair
        t = grid.zoomCamera(Distance(0.0,0.0,1.0), 1.25, 241.0, 226.0)
        t = grid.zoomCamera(Distance(-48.19999999999999,-45.19999999999999,1.25), 1.25, 241.0, 226.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{1}: 0x0 of 2] Distance(-108.44999999999999,-101.69999999999999,1.5625) --> [{4}: -1x-1 of 2] Distance(-8.449999999999989,-1.6999999999999886,1.5625)
        assertDistance(Distance(206.368,185.408,0.64), grid.absolute(c, t, n1, a1), precision)
        t = grid.zoomCamera(Distance(-8.449999999999989,-1.6999999999999886,1.5625), 1.25, 241.0, 226.0)
        t = grid.zoomCamera(Distance(-83.76249999999999,-72.32499999999999,1.953125), 1.25, 241.0, 226.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{4}: -1x-1 of 2] Distance(-177.90312500000005,-160.60625000000005,2.44140625) --> [{5}: -2x-2 of 2] Distance(-77.90312500000005,-60.606250000000045,2.44140625)
        assertDistance(Distance(201.48352000000003,183.74912,0.4096), grid.absolute(c, t, n1, a1), precision)
        t = grid.zoomCamera(Distance(-77.90312500000005,-60.606250000000045,2.44140625), 1.25, 241.0, 226.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{5}: -2x-2 of 2] Distance(-195.57890625000005,-170.95781250000005,3.0517578125) --> [{6}: -3x-3 of 2] Distance(-95.57890625000006,-70.95781250000006,3.0517578125)
        assertDistance(Distance(199.74681600000002,183.159296,0.32768), grid.absolute(c, t, n1, a1), precision)
        t = grid.zoomCamera(Distance(-95.57890625000006,-70.95781250000006,3.0517578125), 1.25, 241.0, 226.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{6}: -3x-3 of 2] Distance(-242.67363281250005,-208.89726562500005,3.814697265625) --> [{7}: -5x-5 of 2] Distance(-42.67363281250002,-8.897265625000017,3.814697265625)
        assertDistance(Distance(198.3574528,182.6874368,0.262144), grid.absolute(c, t, n1, a1), precision)
        t = grid.zoomCamera(Distance(-42.67363281250002,-8.897265625000017,3.814697265625), 1.25, 239.0, 223.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{7}: -5x-5 of 2] Distance(-225.01616210937505,-179.03276367187505,4.76837158203125) --> [{8}: -7x-6 of 2] Distance(-25.016162109375017,-79.0327636718751,4.76837158203125)
        assertDistance(Distance(196.92596224000005,181.82994944,0.2097152), grid.absolute(c, t, n1, a1), precision)
        pair = grid.newElement(c, t, 252.0, 222.0)
        val (n2, a2) = pair
        t = grid.zoomCamera(Distance(-25.016162109375017,-79.0327636718751,4.76837158203125), 1.25, 218.0, 238.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{8}: -7x-6 of 2] Distance(-232.91716308593755,-306.00725097656255,5.9604644775390625) --> [{10}: -9x-9 of 2] Distance(-32.91716308593766,-6.0072509765625455,5.9604644775390625)
        assertDistance(Distance(192.42076979200004,183.54395955199996,0.16777216), grid.absolute(c, t, n1, a1), precision)
        // [{8}: -7x-6 of 2] Distance(-232.91716308593755,-306.00725097656255,5.9604644775390625) --> [{10}: -9x-9 of 2] Distance(-32.91716308593766,-6.0072509765625455,5.9604644775390625)
        assertDistance(Distance(236.48,215.68,0.8), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-32.91716308593766,-6.0072509765625455,5.9604644775390625), 1.25, 218.0, 238.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{10}: -9x-9 of 2] Distance(-292.7934143066409,-289.7253601074219,7.450580596923828) --> [{11}: -11x-11 of 2] Distance(-92.79341430664087,-89.72536010742189,7.450580596923828)
        assertDistance(Distance(188.81661583360003,184.9151676416,0.134217728), grid.absolute(c, t, n1, a1), precision)
        // [{10}: -9x-9 of 2] Distance(-292.7934143066409,-289.7253601074219,7.450580596923828) --> [{11}: -11x-11 of 2] Distance(-92.79341430664087,-89.72536010742189,7.450580596923828)
        assertDistance(Distance(224.064,210.624,0.64), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-92.79341430664087,-89.72536010742189,7.450580596923828), 1.25, 218.0, 238.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{11}: -11x-11 of 2] Distance(-417.6387283325198,-444.37299652099614,9.313225746154785) --> [{12}: -15x-15 of 2] Distance(-17.63872833251986,-44.372996520996196,9.313225746154785)
        assertDistance(Distance(185.93329266688002,186.01213411328,0.1073741824), grid.absolute(c, t, n1, a1), precision)
        // [{11}: -11x-11 of 2] Distance(-417.6387283325198,-444.37299652099614,9.313225746154785) --> [{12}: -15x-15 of 2] Distance(-17.63872833251986,-44.372996520996196,9.313225746154785)
        assertDistance(Distance(214.1312,206.57920000000001,0.512), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-17.63872833251986,-44.372996520996196,9.313225746154785), 1.25, 218.0, 238.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{12}: -15x-15 of 2] Distance(-423.69537086486844,-487.68254203796414,11.641532182693481) --> [{13}: -19x-19 of 2] Distance(-23.69537086486838,-87.68254203796408,11.641532182693481)
        assertDistance(Distance(183.62663413350398,186.88970729062402,0.08589934592), grid.absolute(c, t, n1, a1), precision)
        // [{12}: -15x-15 of 2] Distance(-423.69537086486844,-487.68254203796414,11.641532182693481) --> [{13}: -19x-19 of 2] Distance(-23.69537086486838,-87.68254203796408,11.641532182693481)
        assertDistance(Distance(206.18496,203.34336,0.4096), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-23.69537086486838,-87.68254203796408,11.641532182693481), 1.25, 218.0, 238.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{13}: -19x-19 of 2] Distance(-531.2661740303042,-641.8194739341739,14.551915228366852) --> [{14}: -24x-25 of 2] Distance(-31.26617403030434,-41.81947393417386,14.551915228366852)
        assertDistance(Distance(181.78130730680323,187.59176583249922,0.068719476736), grid.absolute(c, t, n1, a1), precision)
        // [{13}: -19x-19 of 2] Distance(-531.2661740303042,-641.8194739341739,14.551915228366852) --> [{14}: -24x-25 of 2] Distance(-31.26617403030434,-41.81947393417386,14.551915228366852)
        assertDistance(Distance(199.827968,200.75468800000004,0.32768), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-31.26617403030434,-41.81947393417386,14.551915228366852), 1.25, 218.0, 237.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{14}: -24x-25 of 2] Distance(-665.729677987099,-731.5802557587626,18.189894035458565) --> [{15}: -30x-32 of 2] Distance(-65.72967798709897,-31.58025575876252,18.189894035458565)
        assertDistance(Distance(180.30504584544258,187.9934126659994,0.0549755813888), grid.absolute(c, t, n1, a1), precision)
        // [{14}: -24x-25 of 2] Distance(-665.729677987099,-731.5802557587626,18.189894035458565) --> [{15}: -30x-32 of 2] Distance(-65.72967798709897,-31.58025575876252,18.189894035458565)
        assertDistance(Distance(194.7423744,198.5237504,0.262144), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-65.72967798709897,-31.58025575876252,18.189894035458565), 1.25, 218.0, 237.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{15}: -30x-32 of 2] Distance(-858.8090579330924,-893.7812330394981,22.737367544323206) --> [{16}: -38x-40 of 2] Distance(-58.80905793309228,-93.78123303949803,22.737367544323206)
        assertDistance(Distance(179.12403667635405,188.31473013279947,0.04398046511104), grid.absolute(c, t, n1, a1), precision)
        // [{15}: -30x-32 of 2] Distance(-858.8090579330924,-893.7812330394981,22.737367544323206) --> [{16}: -38x-40 of 2] Distance(-58.80905793309228,-93.78123303949803,22.737367544323206)
        assertDistance(Distance(190.67389952,196.73900032,0.2097152), grid.absolute(c, t, n2, a2), precision)
        t = grid.zoomCamera(Distance(-58.80905793309228,-93.78123303949803,22.737367544323206), 1.25, 212.0, 226.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{16}: -38x-40 of 2] Distance(-1022.8734418123959,-1121.510246042907,28.421709430404007) --> [{17}: -48x-51 of 2] Distance(-22.873441812396095,-21.510246042907283,28.421709430404007)
        assertDistance(Distance(177.21922934108323,186.81178410623957,0.035184372088832), grid.absolute(c, t, n1, a1), precision)
        // [{16}: -38x-40 of 2] Distance(-1022.8734418123959,-1121.510246042907,28.421709430404007) --> [{17}: -48x-51 of 2] Distance(-22.873441812396095,-21.510246042907283,28.421709430404007)
        assertDistance(Distance(186.45911961599998,193.55120025600004,0.16777216), grid.absolute(c, t, n2, a2), precision)
        pair = grid.newElement(c, t, 145.0, 178.0)
        val (n3, a3) = pair
        t = grid.zoomCamera(Distance(-22.873441812396095,-21.510246042907283,28.421709430404007), 1.25, 176.0, 204.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{17}: -48x-51 of 2] Distance(-1023.3176137626169,-1181.1159908033906,35.52713678800501) --> [{19}: -58x-62 of 2] Distance(-23.317613762617157,-81.11599080339079,35.52713678800501)
        assertDistance(Distance(169.9353834728666,182.08942728499167,0.0281474976710656), grid.absolute(c, t, n1, a1), precision)
        // [{17}: -48x-51 of 2] Distance(-1023.3176137626169,-1181.1159908033906,35.52713678800501) --> [{19}: -58x-62 of 2] Distance(-23.317613762617157,-81.11599080339079,35.52713678800501)
        assertDistance(Distance(177.32729569279996,187.4809602048,0.134217728), grid.absolute(c, t, n2, a2), precision)
        // [{17}: -48x-51 of 2] Distance(-1023.3176137626169,-1181.1159908033906,35.52713678800501) --> [{19}: -58x-62 of 2] Distance(-23.317613762617157,-81.11599080339079,35.52713678800501)
        assertDistance(Distance(144.16,175.04,0.8), grid.absolute(c, t, n3, a3), precision)
        t = grid.zoomCamera(Distance(-23.317613762617157,-81.11599080339079,35.52713678800501), 1.25, 178.0, 205.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{19}: -58x-62 of 2] Distance(-1288.0836834155953,-1537.728599111596,44.40892098500626) --> [{20}: -70x-77 of 2] Distance(-88.08368341559526,-37.72859911159571,44.40892098500626)
        assertDistance(Distance(164.42830677829326,178.47154182799332,0.02251799813685248), grid.absolute(c, t, n1, a1), precision)
        // [{19}: -58x-62 of 2] Distance(-1288.0836834155953,-1537.728599111596,44.40892098500626) --> [{20}: -70x-77 of 2] Distance(-88.08368341559526,-37.72859911159571,44.40892098500626)
        assertDistance(Distance(170.34183655424,182.78476816384,0.1073741824), grid.absolute(c, t, n2, a2), precision)
        // [{19}: -58x-62 of 2] Distance(-1288.0836834155953,-1537.728599111596,44.40892098500626) --> [{20}: -70x-77 of 2] Distance(-88.08368341559526,-37.72859911159571,44.40892098500626)
        assertDistance(Distance(143.808,172.832,0.64), grid.absolute(c, t, n3, a3), precision)
        t = grid.zoomCamera(Distance(-88.08368341559526,-37.72859911159571,44.40892098500626), 1.25, 179.0, 206.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{20}: -70x-77 of 2] Distance(-1677.9230546788194,-1867.376143693853,55.51115123125783) --> [{21}: -86x-95 of 2] Distance(-77.9230546788192,-67.37614369385369,55.51115123125783)
        assertDistance(Distance(160.18264542263464,175.73723346239464,0.018014398509481985), grid.absolute(c, t, n1, a1), precision)
        // [{20}: -70x-77 of 2] Distance(-1677.9230546788194,-1867.376143693853,55.51115123125783) --> [{21}: -86x-95 of 2] Distance(-77.9230546788192,-67.37614369385369,55.51115123125783)
        assertDistance(Distance(164.913469243392,179.187814531072,0.08589934592), grid.absolute(c, t, n2, a2), precision)
        // [{20}: -70x-77 of 2] Distance(-1677.9230546788194,-1867.376143693853,55.51115123125783) --> [{21}: -86x-95 of 2] Distance(-77.9230546788192,-67.37614369385369,55.51115123125783)
        assertDistance(Distance(143.68640000000002,171.2256,0.512), grid.absolute(c, t, n3, a3), precision)
        t = grid.zoomCamera(Distance(-77.9230546788192,-67.37614369385369,55.51115123125783), 1.25, 179.0, 206.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{21}: -86x-95 of 2] Distance(-2065.2222687578487,-2354.4355744216755,69.38893903907228) --> [{24}: -6x-18 of 23] Distance(-65.22226875784872,-54.43557442167548,69.38893903907228)
        assertDistance(Distance(156.7861163381077,173.54978676991573,0.014411518807585587), grid.absolute(c, t, n1, a1), precision)
        // [{21}: -86x-95 of 2] Distance(-2065.2222687578487,-2354.4355744216755,69.38893903907228) --> [{24}: -6x-18 of 23] Distance(-65.22226875784872,-54.43557442167548,69.38893903907228)
        assertDistance(Distance(160.57077539471356,176.31025162485759,0.068719476736), grid.absolute(c, t, n2, a2), precision)
        // [{21}: -86x-95 of 2] Distance(-2065.2222687578487,-2354.4355744216755,69.38893903907228) --> [{24}: -6x-18 of 23] Distance(-65.22226875784872,-54.43557442167548,69.38893903907228)
        assertDistance(Distance(143.58912,169.94047999999998,0.4096), grid.absolute(c, t, n3, a3), precision)
        t = grid.zoomCamera(Distance(-65.22226875784872,-54.43557442167548,69.38893903907228), 1.25, 179.0, 206.0)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        // [{24}: -6x-18 of 23] Distance(-2549.3462863566365,-2913.2598628314536,86.73617379884035) --> [{25}: -31x-47 of 23] Distance(-49.34628635663648,-13.25986283145312,86.73617379884035)
        assertDistance(Distance(154.06889307048615,171.79982941593258,0.011529215046068469), grid.absolute(c, t, n1, a1), precision)
        // [{24}: -6x-18 of 23] Distance(-2549.3462863566365,-2913.2598628314536,86.73617379884035) --> [{25}: -31x-47 of 23] Distance(-49.34628635663648,-13.25986283145312,86.73617379884035)
        assertDistance(Distance(157.09662031577085,174.00820129988605,0.0549755813888), grid.absolute(c, t, n2, a2), precision)
        // [{24}: -6x-18 of 23] Distance(-2549.3462863566365,-2913.2598628314536,86.73617379884035) --> [{25}: -31x-47 of 23] Distance(-49.34628635663648,-13.25986283145312,86.73617379884035)
        assertDistance(Distance(143.511296,168.91238399999997,0.32768), grid.absolute(c, t, n3, a3), precision)
        t = grid.zoomCamera(Distance(-49.34628635663648,-13.25986283145312,86.73617379884035), 1.25, 179.0, 206.0)
        val (c2, t2) = (c, t)
        optimised = grid.validateCamera(c, t)
        c = optimised._1
        t = optimised._2
        val (c3, t3) = (c, t)
        val g1 = grid.absolute(c2, t2, n1, a1)
        val g2 = grid.absolute(c3, t3, n1, a1)
        assertDistance(g1, g2, precision)
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
        val cameraAbsolute = Distance(12, 34, 1).asCameraNode
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
        val cameraAbsolute = Distance(0, 0, 0.5).asCameraNode
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
        val camera = Distance(12, 34, 1).asCameraNode
        val element1 = Distance(56, 78, 1)
        val element2 = Distance(90, -34, 1)
        val gui1 = grid.absolute(root, camera, root, element1)
        assertDistance(Distance(12 + 56, 34 + 78, 1), gui1)
        val gui2 = grid.absolute(root, camera, root, element2)
        assertDistance(Distance(12 + 90, 34 - 34, 1), gui2)
      }
      "sum only absolute, when on same node with zooming" in {
        val (root, grid) = rootGridPair()
        val camera = Distance(12, 34, 0.25).asCameraNode
        val element1 = Distance(56, 78, 1.5)
        val element2 = Distance(90, -34, 0.5)
        val gui1 = grid.absolute(root, camera, root, element1)
        assertDistance(Distance((12 + 56) * 0.25, (34 + 78) * 0.25, 0.25 * 1.5), gui1)
        val gui2 = grid.absolute(root, camera, root, element2)
        assertDistance(Distance((12 + 90) * 0.25, (34 - 34) * 0.25, 0.25 * 0.5), gui2)
      }
      "calculate gui absolute, when camera and elements in different nodes" in {
        val (camera, grid) = rootGridPair()
        val cameraAbsolute = Distance(0, 0, 1).asCameraNode
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
        val cameraAbsolute = Distance(-10, -20, 1).asCameraNode
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
        val cameraAbsolute = Distance(0, 0, 0.5).asCameraNode
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
      s"Expeced ${expected} +- ${t}, but actual ${actual}\t${getErrorPosition}\n")
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

  private def getErrorPosition(): String = {
    try {
      val stack = Thread.currentThread().getStackTrace()
      val element = stack(3)
      s"(${element.getFileName}:${element.getLineNumber}})\t"
    } catch {
      case e: Exception => "Stack trace went wrong"
    }
  }

  private val precision = Some(1E-7)

}