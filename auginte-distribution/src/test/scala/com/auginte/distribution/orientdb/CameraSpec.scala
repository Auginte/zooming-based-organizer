package com.auginte.distribution.orientdb

import com.auginte.distribution.orientdb.TestDbHelpers._
import com.auginte.test.UnitSpec
import scala.collection.JavaConversions._

/**
 * Unit tests for [[com.auginte.distribution.orientdb.Camera]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class CameraSpec extends UnitSpec {
  "OrientDB Camera storage" when {
    "getting root node" should {
      "create default Camera->View->Node in new database" in {
        val db = newDb
        assert(0 == db.countVertices("Node"))
        assert(0 == db.countVertices("Camera"))
        val camera = Camera.mainCamera(db)
        assert(1 == db.countVertices("Node"))
        assert(1 == db.countVertices("Camera"))
        val node = Position.rootNode(db)
        assert(node === camera.node)
      }
      "reuse first Camera in existing database" in {
        val db = newDb
        val rawNode = db.addVertex("class:Node", "x", Byte.box(0), "y", Byte.box(0))
        val defaults = Seq("x", Double.box(0), "y", Double.box(0), "scale", Double.box(0))
        val rawCamera = db.addVertex("class:Camera", defaults: _*)
        rawCamera.addEdge("View", rawNode)
        val camera = Camera.mainCamera(db)
        assert(rawCamera === camera.persisted.get)
        assert(rawNode === camera.node.persisted.get)
      }
    }
    "changing attached node" should {
      "attach root node as old one, if setting node first time" in {
        val db = newDb
        val nodeVertex = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(0))
        val node = Node(nodeVertex)
        val defaults = Seq("x", Double.box(0), "y", Double.box(0), "scale", Double.box(0))
        val cameraVertex = db.addVertex("class:Camera", defaults: _*)
        val camera = Camera(cameraVertex)
        assert(nodeVertex === camera.node.persisted.get)
        assert(false === select("SELECT first(out('View')).x AS x FROM Camera").iterator().hasNext)
        camera.node = node
        assert(nodeVertex === camera.node.persisted.get)
        assert(1 === select("SELECT first(out('View')).x AS x FROM Camera").toList.head.field[Byte]("x"))
      }
      "replace edges between Node and Camera" in {
        val db = newDb
        val nodeVertex1 = db.addVertex("class:Node", "x", Byte.box(2), "y", Byte.box(0))
        val nodeVertex2 = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(1))
        nodeVertex2.addEdge("Parent", nodeVertex1)
        val defaults = Seq("x", Double.box(0), "y", Double.box(0), "scale", Double.box(0))
        val cameraVertex = db.addVertex("class:Camera", defaults: _*)
        cameraVertex.addEdge("View", nodeVertex1)
        assert(2 === select("SELECT first(out('View')).x AS x FROM Camera").toList.head.field[Byte]("x"))
        val node1 = Node(nodeVertex1)
        val node2 = Node(nodeVertex2)
        val camera = Camera(cameraVertex)
        camera.node = node1
        assert(2 === select("SELECT first(out('View')).x AS x FROM Camera").toList.head.field[Byte]("x"))
        camera.node = node2
        assert(1 === select("SELECT first(out('View')).x AS x FROM Camera").toList.head.field[Byte]("x"))
      }
    }
  }
}