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

  "Grid structure" when {
    "creating new" should {
      "provide root element" in {
        val root: Node = standardGrid().root
      }
    }
    "using existing" should {
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
  }

  "Absolute -> Nodes converter" when {
    "distance is small" should {
      "return root node" in {
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
    }
    "simple zoomed" should {
      val (root, grid) = rootGridPair()
      val n1 = grid.getNode(root, 0, 0, 1 / 100.0)
      val n2 = grid.getNode(root, 1, 2, 1 / 102.0)
      val n3 = grid.getNode(n2, 1, 2, 1 / 199.9)
      val n4 = grid.getNode(n3, -10, -3, 1 / 123.4)
      "create children" in {
        assert(n1.parent.get === root)
        assert(n2.parent.get === root)
        assert(n3.parent.get === n2)
        assert(n4.parent.get !== n3)
      }
      "use coordinates from translation" in {
        assertXY(n1, 0, 0)
        assertXY(n2, 1, 2)
        assertXY(n3, 1, 2)
        assertXY(n4, 90, 97)
      }
    }
    "simple scaled" should {
      val (root, grid) = rootGridPair()
      val n1 = grid.getNode(root, 0, 0, 100.0)
      val n2 = grid.getNode(root, 1, 2, 102.0)
      val n3 = grid.getNode(n2, 1, 2, 199.9)
      val n4 = grid.getNode(n3, -10, -3, 123.4)
      "create parents" in {
        assert(root.parent.get === n1)
        assert(root.parent.get === n2)
        assert(n2.parent.get === n3)
        assert(n3.parent.get === n4)
        assert(n2.parent.get !== n4)
        assert(root.parent.get !== n4)
      }
      "use 0 (straight up) coordinates" in {
        assertXY(n1, 0, 0)
        assertXY(n2, 0, 0)
        assertXY(n3, 0, 0)
        assertXY(n4, 0, 0)
      }
    }
    "translated" when {
      "only positive" should {
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
        "create new parents" in {
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
        }
        "use positive (same level) coordinates" in {
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
        }
        "split coordinates for long translations" in {
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
      }
      "only negative" should {
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
        "create new parents" in {
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
        }
        "use negative (same level) coordinates" in {
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
        }
        "split coordinates for long translations" in {
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
      }
      "positive and negative" when {
        "around root" should {
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
          "create new parents for elements from center" in {
            assert(root.parent.get === r1)
            assert(n1.parent.get === r1)
            assert(n2.parent.get === r1)
            assert(n3.parent.get === r1)
            assert(n4.parent.get === r1)
          }
          "use translation coordinates for elements from center" in {
            assertXY(n1, 1, 2)
            assertXY(n2, -3, 4)
            assertXY(n3, -5, -6)
            assertXY(n4, 7, -8)
          }
          val n5 = grid.getNode(n4, 123, 999, 1)
          val n6 = grid.getNode(n1, -234, 333, 1)
          val n7 = grid.getNode(n2, -111, -888, 1)
          val n8 = grid.getNode(n3, 666, -111, 1)
          val n9 = grid.getNode(n4, -111, 222, 1)
          "create new parents for elements from other positions" in {
            assert(n5.parent.get === r1)
            assert(n6.parent.get === r1)
            assert(n7.parent.get === r1)
            assert(n8.parent.get === r1)
            assert(n9.parent.get === r1)
          }
          "swap signs, if translation cross axis" in {
            assertXY(n5, 8, 1)
            assertXY(n6, -1, 5)
            assertXY(n7, -4, -4)
            assertXY(n8, 1, -7)
            assertXY(n9, 6, -6)
          }
        }
        "in deep positive edge situations" should {
          val (root, grid) = rootGridPair()
          //  root
          //   \
          //    n1_________________________              (1;1)
          //    |                          \
          //    n2_________                 n13          (50;50)                                      (51;51)
          //    |  \   \   \                |
          //    n3  n5  n7  n9_______       n14_         (98;98) (98;99) (99;98) (99;99)              (0;0)
          //    |   |   |   |   \    \      |   \
          //    n4  n6  n8  n10  n11  n12   n15  n16     (99;99) (99;0)  (0; 99) (0,0) (1,1) (99,99)  (0,0) (1,1)
          //               <->             <->
          //
          //         +------+-------+  +----+----+
          //         | n4   |  n6   |  |n12 |    |
          //         +-----n10------+  +---n15---+
          //         | n8   |  n11  |  |    | n16|
          //         +----- +-------+  +----+----+
          val n1 = grid.getNode(root, 1, 1, 0.01)
          val n2 = grid.getNode(n1, 50, 50, 0.01)
          val n10 = grid.getNode(n2, 99, 99, 0.0001)
          val n9 = n10.parent.getOrElse(invalid)

          val n13 = grid.getNode(n2, 100, 100, 1)
          val n14 = grid.getNode(n13, 0, 0, 0.01)
          val n15 = grid.getNode(n14, 0, 0, 0.01)

          val n4 = grid.getNode(n10, -100, -100, 0)
          val n6 = grid.getNode(n10, 0, -100, 0)
          val n8 = grid.getNode(n10, -100, 0, 0)
          val n11 = grid.getNode(n10, 100, 100, 0)
          val n3 = n4.parent.getOrElse(invalid)
          val n5 = n6.parent.getOrElse(invalid)
          val n7 = n8.parent.getOrElse(invalid)

          val n12 = grid.getNode(n15, -100, -100, 0)
          val n16 = grid.getNode(n15, 100, 100, 0)
          "have main nodes " in {
            assertParents(n10, n9, n2, n1, root)
            assertParents(n15, n14, n13, n1, root)
            assertXY(n1, 1, 1)
            assertXY(n2, 50, 50)
            assertXY(n9, 99, 99)
            assertXY(n10, 0, 0)
          }
          "use positive positions in simple coordinates space" in {
            assertParents(n15, n14, n13, n1, root)
            val n2_2 = grid.getNode(n13, -100, -100, 0)
            assertXY(n13, 51, 51)
            assert(n2 === n2_2)
            assertXY(n14, 0, 0)
            assertXY(n15, 0, 0)
          }
          "use positive positions in edge coordinates space" in {
            assertParents(n4, n3, n2, n1, root)
            assertParents(n6, n5, n2, n1, root)
            assertParents(n8, n7, n2, n1, root)
            assertParents(n11, n9, n2, n1, root)
            assertXY(n4, 99, 99)
            assertXY(n6, 0, 99)
            assertXY(n8, 99, 0)
            assertXY(n11, 1, 1)

            assertParents(n12, n9, n2, n1, root)
            assertParents(n16, n14, n13, n1, root)
            assertXY(n12, 99, 99)
            assertXY(n16, 1, 1)
          }
        }
      }
    }
    "translated with zooming" should {
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
      val r2 = root.parent.getOrElse(invalid)
      val r3 = n5.parent.getOrElse(invalid)
      val r4 = n4.parent.getOrElse(invalid)
      val r6 = n6.parent.getOrElse(invalid)
      val r5 = r6.parent.getOrElse(invalid)
      val r1 = r2.parent.getOrElse(invalid)
      "create child elements" in {
        assert(n1.parent.get === root)
        assert(n2.parent.get === root)
        assert(n3.parent.get === root)
      }
      "create parents for larger translations" in {
        assert(r3.parent.get === r2)
        assert(r4.parent.get === r2)
        assert(r5.parent.get === r1)
        assert(n7.parent.get === n4)
        assert(n8.parent.get === n4)
      }
      "use translation coordinates by zoom level" in {
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
      }
      "split coordinates for long translations" in {
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
    }
    "translated with scaling" should {
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
      "create parent elements" in {
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
      }
      "use translation coordinates by scale level" in {
        assertXY(r1, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r3, 0, 0)
        assertXY(r4, 0, 0)
        assertXY(n1, 1, 1)
      }
      "split coordinates for long translations" in {
        assertXY(n2, -12, -98)
        assertXY(n3, 12, -87)
        assertXY(n4, 0, 12)
        assertXY(n5, 12, 0)
        assertXY(n6, -12, 0)
      }
    }
    "translated with (complex) zooming and scaling" should {
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
      "create parent or child elements" in {
        assertParents(root, r1, r2, r3, r4)
        assertParents(n1, r21, r31, r4)
        assertParents(n11, n1, r21, r31, r4)
        assertParents(n111, n11, n1, r21, r31, r4)
        assertParents(n1111, n111, n11, n1, r21, r31, r4)
        assertParents(n2, r22, r31, r4)
        assertParents(n3, r32, r4)
        assertParents(n4, r32, r4)
        assertParents(n5, r4)
      }
      "use translation coordinates by scale level" in {
        assertXY(root, 0, 0)
        assertXY(r1, 0, 0)
        assertXY(r2, 0, 0)
        assertXY(r3, 0, 0)
        assertXY(r4, 0, 0)
      }
      "split coordinates for long translations" in {
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
      }
      "switch parent node by closest new coordinate" in {
        assertXY(n4, 23 + 34, 12 + 87)
        assertXY(n5, 1 + 12, 1)
      }
    }
    "scaled large value" should {
      val (oldRoot, grid) = rootGridPair()
      var parent = grid.root
      val deep = 123 // 12345 in ~11 s.
      var top = grid.root
      "create multilevel hierarchy" in {
        for (i <- 1 to deep) {
          grid.getNode(grid.root, 0, 0, 100)
        }
        top = grid.root
      }
      "iterate back to lower level" in {
        var parent = grid.root
        for (i <- 1 to deep) {
          parent = grid.getNode(parent, 0, 0, 1 / 100.0)
        }
        assert(parent === oldRoot)
        assert(top === grid.root)
      }
      "compare textual distance representation" in {
        val scale = "1" + ("00" * deep)
        assertResult(("0", "0", scale)) {
          grid.absoluteTextual(parent, top)
        }
      }
    }
    "translate large value" should {
      val (oldRoot, grid) = rootGridPair()
      //  root
      //  :
      //  |______            r2         (0,0)
      //  |\____ \____       r1         (0,0)          (1, 0)     (99, 49)
      //  | ....  ..... .... translated (0,0) (50, 25) (0, 50) .. (50, 75)
      //
      val deep = 100 * 2 - 1 // 100*100*2-1 in ~4 s. (99,49)<-(99,99)<-(50,75)
      var translated = oldRoot
      "create multilevel hierarchy" in {
        for (i <- 1 to deep) {
          translated = grid.getNode(translated, 5000, 2500, 1)
        }
        val r1 = translated.parent.getOrElse(invalid)
        val r2 = r1.parent.getOrElse(invalid)
        assert(r2 === grid.root)
      }
      "compare textual distance representation" in {
        assertResult(("9950", "4975", "10000")) {
          grid.absoluteTextual(translated, grid.root)
        }
      }
    }
  }

  "Nodes -> Absolute converter" when {
    "same nodes" should {
      "return zero distance" in {
        val (root, grid) = rootGridPair()
        val absolute = grid.absoluteBetweenFirst(root, root)
        assert(Distance(0, 0, 1) === absolute)
      }
    }
    "straight child to parent relation" should {
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
      "return scale value larger than grid size " in {
        assert(Distance(0, 0, 100) === grid.absoluteBetweenFirst(child1, root))
        assert(Distance(0, 0, 10000) === grid.absoluteBetweenFirst(child2, root))
        assert(Distance(0, 0, 1000000) === grid.absoluteBetweenFirst(child3, root))
        assert(Distance(0, 0, 10000) === grid.absoluteBetweenFirst(child3, child1))
      }
    }
    "straight parent to child relation" should {
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
      "return scale value smaller than grid size" in {
        assert(Distance(0, 0, 0.01) === grid.absoluteBetweenFirst(root, child1))
        assert(Distance(0, 0, 0.0001) === grid.absoluteBetweenFirst(root, child2))
        assert(Distance(0, 0, 0.000001) === grid.absoluteBetweenFirst(root, child3))
        assert(Distance(0, 0, 0.0001) === grid.absoluteBetweenFirst(child1, child3))
      }
    }
    "same scale level relation" should {
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
      "return translation value for center-translated nodes" in {
        assert(Distance(100, 0, 1) === grid.absoluteBetweenFirst(root, n1))
        assert(Distance(-9900, 9900, 1) === grid.absoluteBetweenFirst(root, n2))
        assert(Distance(123400, -654300, 1) === grid.absoluteBetweenFirst(root, n3))
        assert(Distance(-100, 0, 1) === grid.absoluteBetweenFirst(n1, root))
        assert(Distance(9900, -9900, 1) === grid.absoluteBetweenFirst(n2, root))
        assert(Distance(-123400, 654300, 1) === grid.absoluteBetweenFirst(n3, root))
      }
      "return translation value for transalted-translated nodes" in {
        assert(Distance(-10000, 9900, 1) === grid.absoluteBetweenFirst(n1, n2))
        assert(Distance(10000, -9900, 1) === grid.absoluteBetweenFirst(n2, n1))
        assert(Distance(-123300, 654300, 1) === grid.absoluteBetweenFirst(n3, n1))
        assert(Distance(133300, -664200, 1) === grid.absoluteBetweenFirst(n2, n3))
      }
    }
    "child to uncle relation" should {
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
      "return scale and translation value" in {
        assert(Distance(0, 0, 100) === grid.absoluteBetweenFirst(root, r1))
        assert(Distance(120000, -340000, 100) === grid.absoluteBetweenFirst(root, n1))
        assert(Distance(-990000, 990000, 100) === grid.absoluteBetweenFirst(root, n2))
        assert(Distance(12000000, 43000000, 10000) === grid.absoluteBetweenFirst(root, n3))
      }
    }
    "child to grandchild relation" should {
      "return scale and translation" in {
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
  }

  "Infinity to Graphical user interface converter" when {
    "camera and elements in same level" when {
      "no transformation" should {
        "calculate GUI absolute coordinates" in {
          val (root, grid) = rootGridPair()
          val camera = Distance(12, 34, 1).asCameraNode
          val element1 = Distance(56, 78, 1)
          val element2 = Distance(90, -34, 1)
          val gui1 = grid.absolute(root, camera, root, element1)
          assertDistance(Distance(12 + 56, 34 + 78, 1), gui1)
          val gui2 = grid.absolute(root, camera, root, element2)
          assertDistance(Distance(12 + 90, 34 - 34, 1), gui2)
        }
      }
      "simple zooming" should {
        "sum only absolute" in {
          val (root, grid) = rootGridPair()
          val camera = Distance(12, 34, 0.25).asCameraNode
          val element1 = Distance(56, 78, 1.5)
          val element2 = Distance(90, -34, 0.5)
          val gui1 = grid.absolute(root, camera, root, element1)
          assertDistance(Distance((12 + 56) * 0.25, (34 + 78) * 0.25, 0.25 * 1.5), gui1)
          val gui2 = grid.absolute(root, camera, root, element2)
          assertDistance(Distance((12 + 90) * 0.25, (34 - 34) * 0.25, 0.25 * 0.5), gui2)
        }
      }
    }
    "camera and elements in different nodes" when {
      "same level" should {
        "optimise new element" in {
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
        "calculate GUI absolute coordinates" in {
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
      }
      "moved camera" should {
        "optimise new element" in {
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
        "calculate GUI absolute coordinates" in {
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
      }
      "zoomed camera" should {
        "optimise new element" in {
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
        "calculate GUI absolute coordinates" in {
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
  }

  "Camera optimisation" when {
    "same level" should {
      val (camera, grid) = rootGridPair()
      val translation = Distance(106.0, -1.0, 1.0).asCameraNode
      val newCamera = grid.getCameraNode(camera, translation)
      val (n1, a1, g1) = (grid.getNode(camera, 0, 0, 1), Distance(83.0, 85.0, 1.0), Distance(189.0, 84.0, 1.0))
      val (n2, a2, g2) = (grid.getNode(camera, 200, 0, 1), Distance(18.0, 88.0, 1.0), Distance(324.0, 87.0, 1.0))
      val (n3, a3, g3) = (grid.getNode(camera, 0, 200, 1), Distance(89.0, 13.0, 1.0), Distance(195.0, 212.0, 1.0))
      val (n4, a4, g4) = (grid.getNode(camera, 200, 200, 1), Distance(38.0, 14.0, 1.0), Distance(344.0, 213.0, 1.0))
      val newTranslation = grid.absolute(camera, newCamera, translation)
      "optimise camera absolute and hierarchy" in {
        assertXY(newCamera, -1, 0)
        assertXY(n1, 0, 0)
        assertXY(n2, 2, 0)
        assertXY(n3, 0, 2)
        assertXY(n4, 2, 2)
        assert(Distance(6.0, -1.0, 1.0).asCameraNode === newTranslation)
      }
      "keep GUI coordinates same" in {
        assert(g1 === grid.absolute(camera, translation, n1, a1))
        assert(g2 === grid.absolute(camera, translation, n2, a2))
        assert(g3 === grid.absolute(camera, translation, n3, a3))
        assert(g4 === grid.absolute(camera, translation, n4, a4))
        assert(g1 === grid.absolute(newCamera, newTranslation, n1, a1))
        assert(g2 === grid.absolute(newCamera, newTranslation, n2, a2))
        assert(g3 === grid.absolute(newCamera, newTranslation, n3, a3))
        assert(g4 === grid.absolute(newCamera, newTranslation, n4, a4))
      }
    }
    "camera zoomed" should {
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
      val n1 = grid.getNode(c1, i1)
      val a1 = grid.absoluteNew(c1, n1, i1)
      "keep GUI coordinates same for not optmised" in {
        assertDistance(g1, grid.absolute(c1, t1, c1, i1), precision) // From same point
      }
      "keep GUI coordinates same for node translated" in {
        assertXY(n1, 1, 1)
        assert(Distance(2.0, 6.0, 1.0) === a1)
        assertDistance(g1, grid.absolute(c1, t1, n1, a1), precision) // Element in other node
      }
      "keep GUI coordinates same for camera zoomed" in {
        val c2 = grid.getCameraNode(c1, t1)
        assertXY(c1, 0, 0)
        assertXY(c2, -81, -88)
        assert(c2 isChildOf c1)
        val t2 = grid.absolute(c1, c2, t1)
        assert(Distance(-18.769657879855117, -94.73034632515152, 0.9883452756357822) === t2)
        val gg = grid.absolute(c2, t2, n1, a1)
        assertDistance(g1, gg, precision) // Camera in other node
      }
    }
    "multiple zooming and translation" should {
      // c - camera's node, t - camera's absolute
      // n - element's node, a - element's absolute
      // g - coordinates in GUI
      val (c1, grid) = rootGridPair()
      var g: Distance = Distance()
      var c = c1
      var t = Distance()
      object cameraState {
        def <==(newValue: (Node, Distance)): Unit = {
          c = newValue._1
          t = newValue._2
        }

        def <==(translation: Distance): Unit = {
          t = translation
        }
      }
      val (n1, a1) = grid.newElement(c, t, 214.0, 188.0)
      cameraState <== grid.zoomCamera(Distance(0.0, 0.0, 1.0), 1.25, 241, 226)
      cameraState <== grid.zoomCamera(Distance(-48.19999999999999, -45.19999999999999, 1.25), 1.25, 241, 226)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-8.449999999999989, -1.6999999999999886, 1.5625), 1.25, 241, 226)
      cameraState <== grid.zoomCamera(Distance(-83.76249999999999, -72.32499999999999, 1.953125), 1.25, 241, 226)
      val (c2, t2) = (c, t)
      cameraState <== grid.validateCamera(c, t)
      val (c3, t3) = (c, t)
      cameraState <== grid.zoomCamera(Distance(-77.90312500000005, -60.606250000000045, 2.44140625), 1.25, 241, 226)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-95.57890625000006, -70.95781250000006, 3.0517578125), 1.25, 241, 226)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-42.67363281250002, -8.897265625000017, 3.814697265625), 1.25, 239, 223)
      cameraState <== grid.validateCamera(c, t)

      val (n2, a2) = grid.newElement(c, t, 252.0, 222.0)
      cameraState <== grid.zoomCamera(Distance(-25.016162109375017, -79.0327636718751, 4.76837158203125), 1.25, 218, 238)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-32.91716308593766, -6.0072509765625455, 5.9604644775390625), 1.25, 218, 238)
      val (c4, t4) = (c, t)
      cameraState <== grid.validateCamera(c, t)
      val (c5, t5) = (c, t)
      cameraState <== grid.zoomCamera(Distance(-92.79341430664087, -89.72536010742189, 7.450580596923828), 1.25, 218, 238)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-17.63872833251986, -44.372996520996196, 9.313225746154785), 1.25, 218, 238)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-23.69537086486838, -87.68254203796408, 11.641532182693481), 1.25, 218, 238)
      val (c6, t6) = (c, t)
      cameraState <== grid.validateCamera(c, t)
      val (c7, t7) = (c, t)
      cameraState <== grid.zoomCamera(Distance(-31.26617403030434, -41.81947393417386, 14.551915228366852), 1.25, 218, 237)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-65.72967798709897, -31.58025575876252, 18.189894035458565), 1.25, 218, 237)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-58.80905793309228, -93.78123303949803, 22.737367544323206), 1.25, 212, 226)
      cameraState <== grid.validateCamera(c, t)

      val (n3, a3) = grid.newElement(c, t, 145.0, 178.0)
      cameraState <== grid.zoomCamera(Distance(-22.873441812396095, -21.510246042907283, 28.421709430404007), 1.25, 176, 204)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-23.317613762617157, -81.11599080339079, 35.52713678800501), 1.25, 178, 205)
      val (c8, t8) = (c, t)
      cameraState <== grid.validateCamera(c, t)
      val (c9, t9) = (c, t)
      cameraState <== grid.zoomCamera(Distance(-88.08368341559526, -37.72859911159571, 44.40892098500626), 1.25, 179, 206)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-77.9230546788192, -67.37614369385369, 55.51115123125783), 1.25, 179, 206)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-65.22226875784872, -54.43557442167548, 69.38893903907228), 1.25, 179, 206)
      cameraState <== grid.validateCamera(c, t)
      cameraState <== grid.zoomCamera(Distance(-49.34628635663648, -13.25986283145312, 86.73617379884035), 1.25, 179, 206)
      val (c10, t10) = (c, t)
      cameraState <== grid.validateCamera(c, t)
      val (c11, t11) = (c, t)
      "keep GUI coordinates same after multiple" in {
        assertDistance(grid.absolute(c2, t2, n1, a1), grid.absolute(c3, t3, n1, a1), precision)

        assertDistance(grid.absolute(c4, t4, n1, a1), grid.absolute(c5, t5, n1, a1), precision)
        assertDistance(grid.absolute(c4, t4, n2, a2), grid.absolute(c5, t5, n2, a2), precision)
        assertDistance(grid.absolute(c6, t6, n1, a1), grid.absolute(c7, t7, n1, a1), precision)
        assertDistance(grid.absolute(c6, t6, n2, a2), grid.absolute(c7, t7, n2, a2), precision)

        assertDistance(grid.absolute(c8, t8, n1, a1), grid.absolute(c9, t9, n1, a1), precision)
        assertDistance(grid.absolute(c8, t8, n2, a2), grid.absolute(c9, t9, n2, a2), precision)
        assertDistance(grid.absolute(c8, t8, n3, a3), grid.absolute(c9, t9, n3, a3), precision)
        assertDistance(grid.absolute(c10, t10, n1, a1), grid.absolute(c11, t11, n1, a1), precision)
        assertDistance(grid.absolute(c10, t10, n2, a2), grid.absolute(c11, t11, n2, a2), precision)
        assertDistance(grid.absolute(c10, t10, n3, a3), grid.absolute(c11, t11, n3, a3), precision)
      }
    }
  }

  "Element optimisation" when {
    "translating element" when {
      "in same level" should {
        val (camera, grid) = rootGridPair()
        val transformation = Distance(12, 34, 0.25).asCameraNode
        val element = camera
        val absolute = Distance(10, 20, 1.25)
        val gui = (56.0, 78.9)
        val (newNode, newAbsolute) = grid.translateElement(element, absolute, gui._1, gui._2, camera, transformation)
        "switch node by translation" in {
          val parent = camera.parent.getOrElse(invalid)
          assertParents(newNode, parent)
          assertXY(newNode, 2, 3)
        }
        "use main camera scale" in {
          assertDistance(Distance(34.0, 35.6, 1.25), newAbsolute, precision)
        }
      }
      "in different level" should {
        val (camera1, grid) = rootGridPair()
        val transformation1 = Distance(12, 34, 0.0025).asCameraNode
        val element = camera1
        val absolute = Distance(10, 20, 1.25)
        val gui = (56.0, 78.9)
        val (camera2, transformation2) = grid.validateCamera(camera1, transformation1)
        val (node1, absolute1) = grid.translateElement(element, absolute, gui._1, gui._2, camera1, transformation1)
        val (node2, absolute2) = grid.translateElement(element, absolute, gui._1, gui._2, camera2, transformation2)
        "switch node by translation" in {
          assert(camera1 isChildOf camera2)
          val root = camera2.parent.getOrElse(invalid)
          assert(node1 === node2)
          val r1 = node1.parent.getOrElse(invalid)
          assertParents(node1, r1, root)
          assertXY(r1, 2, 3)
          assertXY(node1, 24, 15)
        }
        "use chain of camera scales" in {
          val expected = Distance(10.0, 80.0, 1.25)
          assertDistance(absolute1, absolute2, precision)
          assertDistance(expected, absolute1, precision)
        }
      }
      "scaling elements" should {
        "switch node by scale" in {
          val (camera, grid) = rootGridPair()
          val absolute = Distance(0, 0, 1.25).asCameraNode
          assert(0.8 === absolute.scale)
          val (node1, transformation1) = grid.newElement(camera, absolute, 10.0, 20.0)
          assert(node1 === camera)
          assert(Distance(8, 16, 0.8) === transformation1)

          val (node2, transformation2) = grid.scaleElement(node1, transformation1, 100 / 0.8)
          assert(node1 isChildOf node2)
          assertXY(node1, 0, 0)
          assertXY(node2, 0, 0)
          assert(Distance(0.08, 0.16, 1) === transformation2)
        }
      }
    }
    "using logical (scale+translation) distance" should {
      // Main hierarchy
      val (camera, grid) = rootGridPair()
      val n1 = grid.getNode(camera, 1, 1, 0.01)
      val n2 = grid.getNode(camera, -1, -1, 0.01)
      val n3 = grid.getNode(camera, 100, 0, 0)
      val n4 = grid.getNode(camera, 0, 100, 0)
      val n5 = grid.getNode(camera, 200, 0, 0)
      val n6 = grid.getNode(camera, 0, 200, 0)
      val n7 = grid.getNode(camera, -100, 0, 0)
      val n8 = grid.getNode(camera, 0, -100, 0)
      val n9 = grid.getNode(camera, -200, 0, 0)
      val n10 = grid.getNode(camera, 0, -200, 0)
      val n11 = grid.getNode(camera, 10000, 0, 100)
      val n12 = grid.getNode(camera, 20000, 0, 100)
      val n13 = grid.getNode(camera, -10000, 0, 100)
      val n14 = grid.getNode(camera, -20000, 0, 100)
      val n15 = grid.getNode(camera, 1000000, 0, 10000)
      val n16 = grid.getNode(camera, 2000000, 0, 10000)
      val n17 = grid.getNode(camera, -1000000, 0, 10000)
      val n18 = grid.getNode(camera, -2000000, 0, 10000)
      val n19 = grid.getNode(camera, 1, 1, 0.0001)
      val n20 = grid.getNode(camera, -1, -1, 0.0001)
      val n21 = grid.getNode(camera, 100, 0, 0.01)
      val n22 = grid.getNode(camera, -100, 0, 0.01)
      val n23 = grid.getNode(camera, 1, 1, 0.000001)
      val n24 = grid.getNode(camera, -1, -1, 0.000001)
      val n25 = grid.getNode(camera, 100, 0, 0.0001)
      val n26 = grid.getNode(camera, -100, 0, 0.0001)
      val n27 = grid.getNode(camera, 0, 100, 0.01)
      val n28 = grid.getNode(camera, 0, -100, 0.01)
      val n29 = grid.getNode(camera, 200, 0, 0.01)
      val n30 = grid.getNode(camera, -200, 0, 0.01)
      val n31 = grid.getNode(camera, 0, 200, 0.01)
      val n32 = grid.getNode(camera, 0, -200, 0.01)
      val n33 = grid.getNode(camera, 10000, 0, 0)
      val n34 = grid.getNode(camera, -10000, 0, 0)
      val n35 = grid.getNode(camera, 1, 1, 0.00000001)
      val n36 = grid.getNode(camera, -1, -1, 0.00000001)
      val n37 = grid.getNode(camera, 1, 1, 0.0000000001)
      val n38 = grid.getNode(camera, -1, -1, 0.0000000001)

      // Edge nodes
      val n39 = grid.getNode(camera, 9900, 0, 0)
      val n40 = grid.getNode(camera, 9996, 0, 0.01)
      val n41 = grid.getNode(camera, 9999, 0, 0.01)
      val n42 = grid.getNode(camera, 10000, 0, 0.01)
      val n43 = grid.getNode(camera, 10001, 0, 0.01)
      val n44 = grid.getNode(camera, 9996, 0, 0.0001)
      val n45 = grid.getNode(camera, 9999, 0, 0.0001)
      val n46 = grid.getNode(camera, 10000, 0, 0.0001)
      val n47 = grid.getNode(camera, 10001, 0, 0.0001)

      // Automatically generated parents
      val r1 = camera.parent.getOrElse(invalid)
      val r2 = r1.parent.getOrElse(invalid)
      val r3 = r2.parent.getOrElse(invalid)

      // Automatically generated children, as a result of scale-translate error
      val e1 = grid.getNode(n12, 0, 0, 0.001)
      val e2 = grid.getNode(n14, 0, 0, 0.001)
      val e3 = grid.getNode(n15, 0, 0, 0.001)
      val e4 = grid.getNode(n16, 0, 0, 0.001)
      val e5 = grid.getNode(n17, 0, 0, 0.001)
      val e6 = grid.getNode(n18, 0, 0, 0.001)
      val e7 = grid.getNode(e3, 0, 0, 0.001)
      val e8 = grid.getNode(e4, 0, 0, 0.001)
      val e9 = grid.getNode(e5, 0, 0, 0.001)
      val e10 = grid.getNode(e6, 0, 0, 0.001)

      // Common nodes group
      val converter = (node: Node) => node
      val all = grid.root :: grid.root.entries
      //        ______________________________r3________________________________________
      //       /   /                          |                                     \   \
      //      n18 n17  _______________________r2________________________________   n15 n16
      //      /  /    /   /                   |                           \     \     \   \
      //    e6 e5  n14 n13   _________________r1_____________________      n11   n12   e3  e4
      //    /  /   /    /   /   /   /   /     |      \   \   \   \   \      \      \    \   \
      // e10 e9  e2   n34  n10 n9  n8  n7  camera     n3  n4  n5  n6  n39    n33_   e1   e7  e8
      //                   |   |   |  |      / \      |   |   |   |   | \    |   \
      //                  n32 n30 n28 n22  n2  n1   n21 n27 n29 n31 n40 n41 n42 n43
      //                              |    |   |     |               |   |   |    |
      //                             n26  n20  n19  n25             n44 n45 n46 n47
      //                                   |   |
      //                                  n24  n23
      //                                   |    |
      //                                  n36   n35
      //                                   |    |
      //                                  n38   n37
      //
      //                          +-----------|------------+          ------------------+------
      //                          |          n10           |                            |
      //                          |          n8            |                            |
      //                          -  n9 n7 camera n3 n4 n5 -->         n31      n40 n41 n42 n43
      //                          |          n4            | x                          |
      //                          |          n6            |           0        96  99  |0  1
      //                          +-----------|------------+          ------------------+-----
      //                                      V y
      "retaining consistent hierarchy" in {
        // Base hierarchy
        assertParents(n37, n35, n23, n19, n1, camera, r1, r2, r3)
        assertParents(n38, n36, n24, n20, n2, camera, r1, r2, r3)
        assertParents(n25, n21, n3, r1, r2, r3)
        assertParents(n26, n22, n7, r1, r2, r3)
        assertParents(n27, n4, r1, r2, r3)
        assertParents(n28, n8, r1, r2, r3)
        assertParents(n29, n5, r1, r2, r3)
        assertParents(n30, n9, r1, r2, r3)
        assertParents(n31, n6, r1, r2, r3)
        assertParents(n32, n10, r1, r2, r3)
        assertParents(n33, n11, r2, r3)
        assertParents(n34, n13, r2, r3)
        assertParents(n12, r2, r3)
        assertParents(n14, r2, r3)
        assertParents(n15, r3)
        assertParents(n16, r3)
        assertParents(n17, r3)
        assertParents(n18, r3)
        assertXY(n1, 1, 1)
        assertXY(n2, -1, -1)
        assertXY(n3, 1, 0)
        assertXY(n4, 0, 1)
        assertXY(n5, 2, 0)
        assertXY(n6, 0, 2)
        assertXY(n7, -1, 0)
        assertXY(n8, 0, -1)
        assertXY(n9, -2, 0)
        assertXY(n10, 0, -2)
        assertXY(n11, 1, 0)
        assertXY(n12, 2, 0)
        assertXY(n13, -1, 0)
        assertXY(n14, -2, 0)
        assertXY(n15, 1, 0)
        assertXY(n16, 2, 0)
        assertXY(n17, -1, 0)
        assertXY(n18, -2, 0)
        val directDown = List(
          n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31, n32, n33, n34, n35, n36, n37, n38
        )
        for (node <- directDown) assertXY(node, 0, 0)

        // Edge situations
        assertParents(n44, n40, n39, r1, r2, r3)
        assertParents(n45, n41, n39, r1, r2, r3)
        assertParents(n46, n42, n33, n11, r2, r3)
        assertParents(n47, n43, n33, n11, r2, r3)
        assertXY(n39, 99, 0)
        assertXY(n40, 96, 0)
        assertXY(n41, 99, 0)
        assertXY(n42, 0, 0)
        assertXY(n43, 1, 0)
        assertXY(n44, 0, 0)
        assertXY(n45, 0, 0)
        assertXY(n46, 0, 0)
        assertXY(n47, 0, 0)
      }
      "filter by scale of children" in {
        val r1ChildrenLevel1 = List(n3, n4, n5, n6, n39, n7, n8, n9, n10)
        val r1ChildrenLevel2 = List(n21, n27, n29, n31, n40, n41, n22, n28, n30, n32)
        val r1ChildrenLevel3 = List(n25, n26, n44, n45)
        val r2ChildrenLevel1 = List(n11, n12, n13, n14)
        val r2ChildrenLevel2 = List(n33, n34, e1, e2)
        val r3ChildrenLevel1 = List(n15, n16, n17, n18)
        val r3ChildrenLevel2 = List(e3, e4, e5, e6)
        val r3ChildrenLevel3 = List(e7, e8, e9, e10)

        val cameraDeep1 = List(camera, n1, n2)
        val cameraDeep2 = List(n19, n20) ::: cameraDeep1
        val cameraDeep3 = List(n23, n24) ::: cameraDeep2
        val cameraDeep4 = List(n36, n35) ::: cameraDeep3
        val cameraDeep5 = List(n37, n38) ::: cameraDeep4

        val r1Deep1 = List(r1, camera) ::: r1ChildrenLevel1
        val r1Deep2 = List(n1, n2) ::: r1ChildrenLevel2 ::: r1Deep1
        val r1Deep3 = List(n19, n20) ::: r1ChildrenLevel3 ::: r1Deep2
        val r1Deep4 = r1Deep3 ::: cameraDeep3
        val r1Deep5 = r1Deep3 ::: cameraDeep4
        val r1Deep6 = r1Deep3 ::: cameraDeep5

        val r2Deep1 = List(r2, r1) ::: r2ChildrenLevel1
        val r2Deep2 = List(camera) ::: r2ChildrenLevel2 ::: r1ChildrenLevel1 ::: r2Deep1
        val r3Deep1 = List(r3, r2) ::: r3ChildrenLevel1
        val r3Deep2 = r3ChildrenLevel2 ::: r2Deep1 ::: r3Deep1
        val r3Deep3 = r3ChildrenLevel3 ::: r2Deep2 ::: r3Deep2

        val filteredCameraDeep1 = grid.filter(all, camera, 0, 1, converter)
        val filteredCameraDeep2 = grid.filter(all, camera, 0, 2, converter)
        val filteredCameraDeep3 = grid.filter(all, camera, 0, 3, converter)
        val filteredCameraDeep4 = grid.filter(all, camera, 0, 4, converter)
        val filteredCameraDeep5 = grid.filter(all, camera, 0, 5, converter)

        val filteredR1Deep1 = grid.filter(all, r1, 0, 1, converter)
        val filteredR1Deep2 = grid.filter(all, r1, 0, 2, converter)
        val filteredR1Deep3 = grid.filter(all, r1, 0, 3, converter)
        val filteredR1Deep4 = grid.filter(all, r1, 0, 4, converter)
        val filteredR1Deep5 = grid.filter(all, r1, 0, 5, converter)
        val filteredR1Deep6 = grid.filter(all, r1, 0, 6, converter)

        val filteredR2Deep1 = grid.filter(all, r2, 0, 1, converter)
        val filteredR2Deep2 = grid.filter(all, r2, 0, 2, converter)
        val filtered32Deep1 = grid.filter(all, r3, 0, 1, converter)
        val filtered32Deep2 = grid.filter(all, r3, 0, 2, converter)
        val filtered32Deep3 = grid.filter(all, r3, 0, 3, converter)
        val filtered32Deep8 = grid.filter(all, r3, 0, 8, converter)

        assert(set(cameraDeep1) === set(filteredCameraDeep1))
        assert(set(cameraDeep2) === set(filteredCameraDeep2))
        assert(set(cameraDeep3) === set(filteredCameraDeep3))
        assert(set(cameraDeep4) === set(filteredCameraDeep4))
        assert(set(cameraDeep5) === set(filteredCameraDeep5))

        assert(set(r1Deep1) === set(filteredR1Deep1))
        assert(set(r1Deep2) === set(filteredR1Deep2))
        assert(set(r1Deep3) === set(filteredR1Deep3))
        assert(set(r1Deep4) === set(filteredR1Deep4))
        assert(set(r1Deep5) === set(filteredR1Deep5))
        assert(set(r1Deep6) === set(filteredR1Deep6))

        assert(set(r2Deep1) === set(filteredR2Deep1))
        assert(set(r2Deep2) === set(filteredR2Deep2))

        assert(set(r3Deep1) === set(filtered32Deep1))
        assert(set(r3Deep2) === set(filtered32Deep2))
        assert(set(r3Deep3) === set(filtered32Deep3))
        assert(set(all) === set(filtered32Deep8))
      }
      "filter by distance from camera in trunk" in {
        val cameraDist1Scale1 = List(n8, n7, camera, n3, n4) ::: List(n28, n22, n2, n1, n21, n27)
        val cameraDist1Scale2 = cameraDist1Scale1 ::: List(n26, n20, n19, n25)
        val cameraDist1Scale3 = cameraDist1Scale2 ::: List(n24, n23)
        val cameraDist2Scale1 = cameraDist1Scale1 ::: List(n10, n9, n5, n6) ::: List(n32, n30, n29, n31)
        val cameraDist2Scale2 = cameraDist2Scale1 ::: cameraDist1Scale2
        val cameraDist2Scale3 = cameraDist2Scale2 ::: cameraDist1Scale3

        val r1Dist1Scale1 = List(n13, r1, n11) ::: List(n34, n10, n9, n8, n7, camera, n3, n4, n5, n6, n39, n33)
        val r1Dist1Scale2 = r1Dist1Scale1 ::: List(n32, n30, n28, n22, n2, n1, n21, n27, n29, n31, n40, n41, n42, n43)
        val r1Dist1Scale3 = r1Dist1Scale2 ::: List(n26, n20, n19, n25, n44, n45, n46, n47)
        val r1Dist2Scale1 = r1Dist1Scale1 ::: List(n14, n12) ::: List(e2, e1)
        val r1Dist2Scale2 = r1Dist2Scale1 ::: r1Dist1Scale2
        val r1Dist2Scale3 = r1Dist2Scale2 ::: r1Dist1Scale3

        val r2Dist1Scale1 = List(n17, r2, n15) ::: List(e5, n14, n13, r1, n11, n12, e3)
        val r2Dist1Scale2 = r2Dist1Scale1 ::: List(e9, e2, n34, n10, n9, n8, n7, camera, n3, n4, n5, n6, n39, n33, e1, e7)
        val r2Dist2Scale1 = r2Dist1Scale1 ::: List(n18, n16) ::: List(e6, e4)
        val r2Dist2Scale2 = r2Dist2Scale1 ::: r2Dist1Scale2 ::: List(e10, e8)


        val filteredCameraDist1Scale1 = grid.filter(all, camera, 1, 1, converter)
        val filteredCameraDist1Scale2 = grid.filter(all, camera, 1, 2, converter)
        val filteredCameraDist1Scale3 = grid.filter(all, camera, 1, 3, converter)
        val filteredCameraDist2Scale1 = grid.filter(all, camera, 2, 1, converter)
        val filteredCameraDist2Scale2 = grid.filter(all, camera, 2, 2, converter)
        val filteredCameraDist2Scale3 = grid.filter(all, camera, 2, 3, converter)

        val filteredR1Dist1Scale1 = grid.filter(all, r1, 1, 1, converter)
        val filteredR1Dist1Scale2 = grid.filter(all, r1, 1, 2, converter)
        val filteredR1Dist1Scale3 = grid.filter(all, r1, 1, 3, converter)
        val filteredR1Dist2Scale1 = grid.filter(all, r1, 2, 1, converter)
        val filteredR1Dist2Scale2 = grid.filter(all, r1, 2, 2, converter)
        val filteredR1Dist2Scale3 = grid.filter(all, r1, 2, 3, converter)

        val filteredR2Dist1Scale1 = grid.filter(all, r2, 1, 1, converter)
        val filteredR2Dist1Scale2 = grid.filter(all, r2, 1, 2, converter)
        val filteredR2Dist2Scale1 = grid.filter(all, r2, 2, 1, converter)
        val filteredR2Dist2Scale2 = grid.filter(all, r2, 2, 2, converter)
        val filteredR3Dist1Scale8 = grid.filter(all, r3, 1, 8, converter)

        assert(set(cameraDist1Scale1) === set(filteredCameraDist1Scale1))
        assert(set(cameraDist1Scale2) === set(filteredCameraDist1Scale2))
        assert(set(cameraDist1Scale3) === set(filteredCameraDist1Scale3))
        assert(set(cameraDist2Scale1) === set(filteredCameraDist2Scale1))
        assert(set(cameraDist2Scale2) === set(filteredCameraDist2Scale2))
        assert(set(cameraDist2Scale3) === set(filteredCameraDist2Scale3))

        assert(set(r1Dist1Scale1) === set(filteredR1Dist1Scale1))
        assert(set(r1Dist1Scale2) === set(filteredR1Dist1Scale2))
        assert(set(r1Dist1Scale3) === set(filteredR1Dist1Scale3))
        assert(set(r1Dist2Scale1) === set(filteredR1Dist2Scale1))
        assert(set(r1Dist2Scale2) === set(filteredR1Dist2Scale2))
        assert(set(r1Dist2Scale3) === set(filteredR1Dist2Scale3))

        assert(set(r2Dist1Scale1) === set(filteredR2Dist1Scale1))
        assert(set(r2Dist1Scale2) === set(filteredR2Dist1Scale2))
        assert(set(r2Dist2Scale1) === set(filteredR2Dist2Scale1))
        assert(set(r2Dist2Scale2) === set(filteredR2Dist2Scale2))
        assert(set(all) === set(filteredR3Dist1Scale8))
      }
      "filter by distance from camera in branch" in {
        val n9Dist1Scale1 = List(n9, n7) ::: List(n30, n22)
        val n9Dist1Scale2 = n9Dist1Scale1 ::: List(n26)

        val n9Dist2Scale1 = n9Dist1Scale1 ::: List(n10, n8, camera, n4, n6) ::: List(n32, n28, n2, n1, n27, n31)
        val n9Dist2Scale2 = n9Dist2Scale1 ::: List(n26, n20, n19)
        val n9Dist2Scale3 = n9Dist2Scale2 ::: List(n24, n23)
        val n9Dist2Scale4 = n9Dist2Scale3 ::: List(n36, n35)
        val n9Dist2Scale5 = n9Dist2Scale4 ::: List(n38, n37)

        val n9Dist3Scale1 = n9Dist2Scale1 ::: List(n3) ::: List(n21)
        val n9Dist3Scale2 = n9Dist3Scale1 ::: n9Dist2Scale2 ::: List(n25)
        val n9Dist4Scale1 = n9Dist3Scale1 ::: List(n5) ::: List(n29)
        val n9Dist4Scale2 = n9Dist4Scale1 ::: n9Dist3Scale2

        val filteredN9Dist1Scale1 = grid.filter(all, n9, 1, 1, converter)
        val filteredN9Dist1Scale2 = grid.filter(all, n9, 1, 2, converter)

        val filteredN9Dist2Scale1 = grid.filter(all, n9, 2, 1, converter)
        val filteredN9Dist2Scale2 = grid.filter(all, n9, 2, 2, converter)
        val filteredN9Dist2Scale3 = grid.filter(all, n9, 2, 3, converter)
        val filteredN9Dist2Scale4 = grid.filter(all, n9, 2, 4, converter)
        val filteredN9Dist2Scale5 = grid.filter(all, n9, 2, 5, converter)

        val filteredN9Dist3Scale1 = grid.filter(all, n9, 3, 1, converter)
        val filteredN9Dist3Scale2 = grid.filter(all, n9, 3, 2, converter)
        val filteredN9Dist4Scale1 = grid.filter(all, n9, 4, 1, converter)
        val filteredN9Dist4Scale2 = grid.filter(all, n9, 4, 2, converter)

        assert(set(n9Dist1Scale1) === set(filteredN9Dist1Scale1))
        assert(set(n9Dist1Scale2) === set(filteredN9Dist1Scale2))

        assert(set(n9Dist2Scale1) === set(filteredN9Dist2Scale1))
        assert(set(n9Dist2Scale2) === set(filteredN9Dist2Scale2))
        assert(set(n9Dist2Scale3) === set(filteredN9Dist2Scale3))
        assert(set(n9Dist2Scale4) === set(filteredN9Dist2Scale4))
        assert(set(n9Dist2Scale5) === set(filteredN9Dist2Scale5))

        assert(set(n9Dist3Scale1) === set(filteredN9Dist3Scale1))
        assert(set(n9Dist3Scale2) === set(filteredN9Dist3Scale2))
        assert(set(n9Dist4Scale1) === set(filteredN9Dist4Scale1))
        assert(set(n9Dist4Scale2) === set(filteredN9Dist4Scale2))
      }
      "filter by distance in edge situations" in {
        val n41Dist1Scale1 = List(n41, n42) ::: List(n45, n46)
        val n41Dist2Scale1 = n41Dist1Scale1 ::: List(n43) ::: List(n47)
        val n41Dist3Scale1 = n41Dist2Scale1 ::: List(n40) ::: List(n44)
        val filteredN41Dist1Scale1 = grid.filter(all, n41, 1, 1, converter)
        val filteredN41Dist2Scale1 = grid.filter(all, n41, 2, 1, converter)
        val filteredN41Dist3Scale1 = grid.filter(all, n41, 3, 1, converter)
        assert(set(n41Dist1Scale1) === set(filteredN41Dist1Scale1))
        assert(set(n41Dist2Scale1) === set(filteredN41Dist2Scale1))
        assert(set(n41Dist3Scale1) === set(filteredN41Dist3Scale1))
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
      s"Expected ${x}x$y, but actual ${node.x}x${node.y} in $node\n")

  def assertWithTolerance(expected: Double, actual: Double, tolerance: Double): Unit =
    assert(expected > actual - tolerance && expected < actual + tolerance,
      s"EExpected $expected +- $tolerance, but actual $actual\n")

  def assertDistance(expected: Distance, actual: Distance, tolerance: Option[Double] = None): Unit = tolerance match {
    case Some(t) => assert(expected.x > actual.x - t && expected.x < actual.x + t &&
      expected.y > actual.y - t && expected.y < actual.y + t &&
      expected.scale > actual.scale - t && expected.scale < actual.scale + t,
      s"EExpected $expected +- $t, but actual $actual\t$getErrorPosition\n")
    case None => assert(expected === actual, s"Expeced $expected, but actual $actual\n")
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
          case Some(nodeParent) =>
            assert(nodeParent === parent,
              formatError(s"Last $node !-> " + parents.mkString("->")))
            assertParents(nodeParent, tail)
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
    override def toString() = "Invalid node"

    override def equals(obj: scala.Any): Boolean = false
  }

  private def getErrorPosition: String = {
    try {
      val stack = Thread.currentThread().getStackTrace
      val element = stack(3)
      s"(${element.getFileName}:${element.getLineNumber}})\t"
    } catch {
      case e: Exception => "Stack trace went wrong"
    }
  }

  private val precision = Some(1E-7)

  private def set(list: Traversable[Node]): Set[Node] = list.toSet
}