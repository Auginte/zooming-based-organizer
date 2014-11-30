package com.auginte.distribution.orientdb

import com.auginte.test.UnitSpec
import com.auginte.zooming.{Node, Grid}
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

/**
 * Unit tests for [[com.auginte.distribution.orientdb.Position]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class PositionSpec extends UnitSpec with PositionSpecHelpers{
  "OrientDB Positions storage" when {
    "using as helpers" should {
      "calculate absolute ids from orientdb path" in {
        assert("," === Position.propertySeparator)
        assert("|" === Position.nodeSeparator)
        val map = Map("#11:6" -> "-6,-6", "#11:5" -> "5,5", "#11:4" -> "4,4", "#11:0" -> "0,0")
        assert("-6,-6" === Position.path2absoluteIds("(#11:6)", map))
        assert("-6,-6|5,5" === Position.path2absoluteIds("(#11:6).in_Parent[0](#11:5)", map))
        assert("-6,-6|5,5|4,4|0,0" === Position.path2absoluteIds("(#11:6).in_Parent[0](#11:5).in_Parent[0](#11:4).in_Parent[0](#11:0)", map))
      }
      "calculate absolute ids from nodes hierarchy" in {
        assert("," === Position.propertySeparator)
        assert("|" === Position.nodeSeparator)
        val grid = newGrid
        val n0 = grid.root
        val n1 = grid.getNode(n0, 1, 1, 0.01)
        val n2 = grid.getNode(n1, 2, 2, 0.01)
        val n3 = grid.getNode(n2, 3, 3, 0.01)
        val n4 = grid.getNode(n1, 4, 4, 0.01)
        assert("0,0" === Position.parents2absoluteIds(n0))
        assert("0,0|1,1" === Position.parents2absoluteIds(n1))
        assert("0,0|1,1|2,2" === Position.parents2absoluteIds(n2))
        assert("0,0|1,1|2,2|3,3" === Position.parents2absoluteIds(n3))
        assert("0,0|1,1|4,4" === Position.parents2absoluteIds(n4))
      }
    }
    "storing data to database" should {
      "have method to compare local and database Grid by positions" in {
        val db = newDb
        val grid = newGrid
        //          n6
        //        n5  n7
        //        n4
        //      root
        //     n1   n2
        //   n3
        val root = grid.root
        val n1 = grid.getNode(root, 0, 0, 0.01)
        val n2 = grid.getNode(root, 99, 99, 0.01)
        val n3 = grid.getNode(n1, -44, -55, 0.01)
        val n4 = grid.getNode(root, 0, 0, 100)
        val n5 = grid.getNode(root, 0, 0, 10000)
        val n6 = grid.getNode(n5, 0, 0, 100)
        val n7 = grid.getNode(n5, 9800, 7600, 1)
        assert(99 === n2.x)
        assert(-44 === n3.x)
        assert(0 === n4.x)
        assert(grid.root === n6)
        assert(98 === n7.x)

        val oRoot = newDbNode(db, 0, 0)
        val on1 = newDbNode(db, 0, 0)
        val on2 = newDbNode(db, 99, 99)
        val on3 = newDbNode(db, -44, -55)
        val on4 = newDbNode(db, 0, 0)
        val on5 = newDbNode(db, 0, 0)
        val on6 = newDbNode(db, 0, 0)
        val on7 = newDbNode(db, 98, 76)
        on3.addEdge("Parent", on1)
        on1.addEdge("Parent", oRoot)
        on2.addEdge("Parent", oRoot)
        oRoot.addEdge("Parent", on4)
        on4.addEdge("Parent", on5)
        on5.addEdge("Parent", on6)
        on7.addEdge("Parent", on6)

        val gridIds = Position.absoluteIds(grid).keys
        val orientDbIds = Position.absoluteIds(db).keys
        assert(orientDbIds === gridIds)
      }
    }
  }
}

trait PositionSpecHelpers {
  def testDbName = s"test${System.currentTimeMillis()}"

  def newDb = Structure.createRepository(testDbName, "memory")

  def newGrid = new Grid

  def newDbNode(db: OrientBaseGraph, x: Byte, y: Byte) =
    db.addVertex("class:Node", "x", Byte.box(x), "y", Byte.box(y))

  // Do not output OLogManager creation and shut down of database
  System.setProperty("log.console.level", "FINE")
}