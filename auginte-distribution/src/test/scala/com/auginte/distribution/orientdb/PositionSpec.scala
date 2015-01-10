package com.auginte.distribution.orientdb

import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.test.UnitSpec
import TestDbHelpers._
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientDynaElementIterable}
import scala.collection.JavaConversions._

/**
 * Unit tests for [[com.auginte.distribution.orientdb.Position]] and [[com.auginte.distribution.orientdb]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class PositionSpec extends UnitSpec {
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
      "store root for empty grid" in {
        val db = newDb
        val grid = newGrid
        val root = grid.root
        assert(0 === root.x)
        assert(0 === root.y)
        assert(List() === root.children)
        db.setUseLightweightEdges(false)
        assert(0 === db.countVertices("Node"))
        assert(0 === db.countEdges("Parent"))
        Position.store(grid, db)
        assert(1 === db.countVertices("Node"))
        assert(0 === db.countEdges("Parent"))
        val rootVetex = db.getVerticesOfClass("Node").iterator().next()
        assert(0 === rootVetex.getProperty[Byte]("x"))
        assert(0 === rootVetex.getProperty[Byte]("y"))
      }
      "create Node vertexes and Parent edges" in {
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

        Position.store(grid, db)
        val gridIds = Position.absoluteIds(grid).keys
        val orientDbIds = Position.absoluteIds(db).keys
        assert(orientDbIds === gridIds)
      }
    }
    "loading data from database" should {
      "do nothing if database empty" in {
        val db = newDb
        val grid = newGrid
        val oldRoot = grid.root

        Position.load(db, grid)
        assert(1 === grid.flatten.size)
        assert(oldRoot === grid.root)
      }
      "create node hierarchy from Node-Parent relations" in {
        val db = newDb
        val grid = newGrid
        //          n6
        //        n5  n7
        //        n4
        //      root
        //     n1   n2
        //   n3
        val oRoot = newDbNode(db, 9, 9)
        val on1 = newDbNode(db, 1, 1)
        val on2 = newDbNode(db, 99, 99)
        val on3 = newDbNode(db, 44, 55)
        val on4 = newDbNode(db, 4, 4)
        val on5 = newDbNode(db, 5, 5)
        val on6 = newDbNode(db, 0, 0)
        val on7 = newDbNode(db, 98, 76)
        on3.addEdge("Parent", on1)
        on1.addEdge("Parent", oRoot)
        on2.addEdge("Parent", oRoot)
        oRoot.addEdge("Parent", on4)
        on4.addEdge("Parent", on5)
        on5.addEdge("Parent", on6)
        on7.addEdge("Parent", on6)

        Position.load(db, grid)
        val gridIds = Position.absoluteIds(grid).keys
        val orientDbIds = Position.absoluteIds(db).keys
        assert(orientDbIds === gridIds)
      }
      "create node and representation objects from fetched data" in {
          //      n1___________
          //     /  \          \
          //    n2  n3___       r4
          //    |    |   \
          //    r1   r2   r3
          val db = newDb
          val nodeCache = new Cache[Node]
          val representationCache = new Cache[RepresentationWrapper]
          val sql = execSql(db) _
          if (db.countVertices() == 0) {
            val n1 = newDbNode(db, 0, 0)
            val n2 = newDbNode(db, -1, -1)
            val n3 = newDbNode(db, 5, 5)
            n2.addEdge("Parent", n1)
            n3.addEdge("Parent", n1)
            val d1 = Text("hello")
            val d2 = Text("word")
            val d3 = Image("some.jpg")
            val d4 = Image("another.png")
            d1.representation = Representation(0, 0, 1)
            d2.representation = Representation(1.1, 2.2, 3.3)
            d3.representation = Representation(0.1, 0.2, 0.3)
            d4.representation = Representation(-1, -2, 0.9)
            val r1 = d1.storeTo(db)
            val r2 = d2.storeTo(db)
            val r3 = d3.storeTo(db)
            val r4 = d4.storeTo(db)
            r1.addEdge("Inside", n2)
            r2.addEdge("Inside", n3)
            r3.addEdge("Inside", n3)
            r4.addEdge("Inside", n1)
          }
          assert(7 === db.countVertices())

          assert(0 === nodeCache.size)
          assert(0 === representationCache.size)
          val creator: Creator = {
            case "Text" => RepresentationWrapper(new Text())
            case "Image" => RepresentationWrapper(new Image())
            case _ => RepresentationWrapper(new Representation())
          }
          val rows = select("SELECT @rid, x, y, in_Parent, in_Inside FROM Node")
          val nodes = Node.load(db, rows)(nodeCache)
          assert(3 === nodeCache.size)
          assert(3 === nodes.size)
          for (node <- nodes) {
            val representations = node.representations(creator)(representationCache).map(_.storage)
            node match {
              case Node(x, y) if x == 0 && y == 0 => // n1
                assert(None === node.parent)
                assert(2 === node.children.size)
                assert(1 === representations.size)
                representations.head match {
                  case Image(path, r) if Representation(-1.0, -2.0, 0.9) == r => assert("another.png" === path)
                  case r => fail(s"Not n1<-r4 representation: $r")
                }
              case Node(x, y) if x == -1 && y == -1 => // n2
                assert(node.parent.isDefined)
                assert(node.children.isEmpty)
                assert(1 === representations.size)
                representations.head match {
                  case Text(text, r) if Representation(0, 0, 1) == r => assert("hello" === text)
                  case r => fail(s"Not n2<-r1 representation: $r")
                }
              case Node(x, y) if x == 5 && y == 5 => // n3
                assert(node.parent.isDefined)
                assert(node.children.isEmpty)
                assert(2 === representations.size)
                representations.head match {
                  case Text(text, r) if Representation(1.1, 2.2, 3.3) == r => assert("word" === text)
                  case Image(path, r) if Representation(0.1, 0.2, 0.3) == r => assert("some.jpg" === path)
                  case r => fail(s"Not n3<-r2, n3<-r3 representation: $r")
                }
              case n => fail(s"Unknown node: $n")
            }
          }
          assert(4 === representationCache.size)
      }
      "return or create root node, if not exists" in {
        val db = newDb
        assert(0 === db.countVertices("Node"))
        val root1 = Position.rootNode(db)
        assert(1 === db.countVertices("Node"))
        val root2 = Position.rootNode(db)
        assert(root1.persisted.get === root2.persisted.get)
      }
    }
    "debuging" should {
      "calculate common parent between nodes" in {
        val db = newDb
        val grid = newGrid
        val script = scriptSql[OrientDynaElementIterable](db) _
        val vertices = script(
          """
            |let left = create vertex Node set x=1, y=2
            |let right = create vertex Node set x=4, y=3
            |let top = create vertex Node set x=0, y=0
            |let leftEdge = create edge Parent from $left to $top
            |let rightEdge = create edge Parent from $right to $top
            |return [$left, $top, $right]
          """.stripMargin).toList
        val left = Node(vertices(0).asInstanceOf[OrientVertex])
        val top = Node(vertices(1).asInstanceOf[OrientVertex])
        val right = Node(vertices(2).asInstanceOf[OrientVertex])
        val common = grid.getCommonParent(left, right)
        assert(top === common)
      }
    }
  }
}