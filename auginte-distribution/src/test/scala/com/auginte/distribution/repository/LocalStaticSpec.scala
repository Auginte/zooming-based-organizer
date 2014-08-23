package com.auginte.distribution.repository

import com.auginte.common.{ WithId, SoftwareVersion }
import com.auginte.distribution.data._
import com.auginte.test.UnitSpec
import com.auginte.zooming._

import scala.io.Source
import scala.reflect.io.File

/**
 * Unit tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticSpec extends UnitSpec {
  "LocalStatic repository" should {
    "include software version number" in {
      val repository = new LocalStatic(emptyGrid, emptyWithIds(0), camerasWithIds(0))
      val description = repository.description
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      assert(description.auginteVersion !== fallbackVersion)
      assert(description.auginteVersion == softwareVersion)
    }
    "include approximated number of elements" in {
      val r1 = new LocalStatic(emptyGrid, emptyWithIds(0), camerasWithIds(0))
      val r2 = new LocalStatic(emptyGrid, emptyWithIds(1), camerasWithIds(1))
      val r3 = new LocalStatic(emptyGrid, emptyWithIds(5), camerasWithIds(0))
      val r4 = new LocalStatic(emptyGrid, emptyWithIds(0), camerasWithIds(6))
      val r5 = new LocalStatic(emptyGrid, emptyWithIds(125), camerasWithIds(63))
      val r6 = new LocalStatic(emptyGrid, emptyWithIds(190), camerasWithIds(100))
      assert(0 === r1.description.elements)
      assert(0 === r1.description.cameras)
      assert(1 === r2.description.elements)
      assert(1 === r2.description.cameras)
      assert(5 === r3.description.elements)
      assert(0 === r3.description.cameras)
      assert(0 === r4.description.elements)
      assert(6 === r4.description.cameras)
      assert(125 === r5.description.elements)
      assert(63 === r5.description.cameras)
      assert(190 === r6.description.elements)
      assert(100 === r6.description.cameras)
    }
  }

  "LocalStatic repository" when {
    "saving empty" should {
      "should still include description and root node" in {
        val repository = new LocalStatic(emptyGrid, empty(0), cameras(0)) with ConsistentVersion
        val output = repository.saveToString
        val expected = readFile("localStatic/empty.json")
        assert(expected == output)
      }
    }
    "saving multiple nodes" should {
      "save ids with prefixes" in {
        //  root--c1
        //   |
        //   n1--e1
        //   |
        //   n2
        val grid = emptyGrid
        val n1 = grid.getNode(grid.root, 1.23, -4.56, 0.01)
        val n2 = grid.getNode(n1, 3.23, 5.56, 0.01)
        val e1 = ZoomableElement(n1)
        val c1 = ZoomableCamera(grid.root)
        val repository = new LocalStatic(grid, list(e1), list(c1)) with ConsistentVersion
        val output = repository.saveToString
        val expected = readFile("localStatic/multiple-nodes.json")
        assert(expected == output)
      }
    }
    "saving with many elements" should {
      "representations should reuse duplicated nodes" in {
        //      root--c1
        //       |
        //    ___n1___
        //   |       |
        //   n2     n3--c2
        //  :       |
        //  e1     n4--e2
        //  :       :
        //  d1     e3
        val grid = emptyGrid
        val n1 = grid.getNode(grid.root, 1.23, -4.56, 0.01)
        val n2 = grid.getNode(n1, 3.23, 5.56, 0.01)
        val n3 = grid.getNode(n1, 99.1, 33.2, 0.01)
        val n4 = grid.getNode(n3, 5, 8, 0.01)
        val e1 = ZoomableElement(n2, Distance(1.2, 3.4, 1.3))
        val e2 = ZoomableText(n4, Distance(0, 0, 1), "E2")
        val e3 = ZoomableText(n4, Distance(0, 0, 1), "E3")
        val c1 = ZoomableCamera(grid.root, Distance(0, 0, 1))
        val c2 = ZoomableCamera(n3, Distance(1, 1.2, 0.987654321))
        val repository = new LocalStatic(grid, list(e1, e2, e3), list(c1, c2)) with ConsistentVersion
        val output = repository.saveToString
        val expected = readFile("localStatic/reusing-nodes.json")
        assert(expected == output)
      }
    }
  }

  //
  // Helpers
  //

  private val invalidDistance: AbsoluteDistance = (Node(0, 0), Distance())

  private val emptyGrid = new Grid {
    override private[auginte] def newNode: NodeToNode = (n) => new Node(n.x, n.y) with LinearIds
  }
  private val packageDir = "/" + getClass.getPackage.getName.replaceAll("\\.", "/") + "/"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    LinearIds.reset()
  }

  private def emptyWithIds(count: Int): Elements = () => for (i <- 1 to count) yield new Data with ZeroPosition

  private def empty(count: Int): Elements = () => for (i <- 1 to count) yield new Data with ZeroPosition with LinearIds {}

  private def camerasWithIds(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera with ZeroPosition {}

  private def cameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera with ZeroPosition with LinearIds {}

  private def list[A](xs: A*) = () => xs.toList

  @throws[Exception]
  private def readFile(name: String, prefix: String = packageDir): String = {
    val resource = getClass.getResource(prefix + name)
    val here = File(".").toAbsolute.path
    require(resource != null, s"Resource not found: $prefix$name from $here")
    Source.fromURL(resource).getLines() mkString "\n"
  }

  trait ZeroPosition extends Data {
    override def node: Node = Node(0, 0)

    override def position: Distance = Distance()
  }

  trait LinearIds extends WithId {
    override val storageId: String = f"${LinearIds.next}%32s".replaceAll(" ", "0")
  }

  trait ConsistentVersion extends LocalStatic {
    override def description: Description = Description(
      Version("0.0.1-FIXTURE"), super.description.elements, super.description.cameras)
  }

  case class ZoomableElement(node: Node, position: Distance = Distance()) extends Data with LinearIds

  case class ZoomableText(node: Node, position: Distance = Distance(), override val text: String) extends Text with LinearIds

  case class ZoomableCamera(node: Node, position: Distance = Distance()) extends Camera with LinearIds

  object LinearIds {
    var id = 0

    def next = {
      id = id + 1
      id
    }

    def reset(): Unit = id = 1
  }

}
