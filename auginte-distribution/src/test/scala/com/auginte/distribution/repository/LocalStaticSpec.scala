package com.auginte.distribution.repository

import java.io.InputStream
import com.auginte.zooming.NodeAssertions
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
class LocalStaticSpec extends UnitSpec with NodeAssertions {
  "LocalStatic repository" should {
    "include software version number" in {
      val repository = new LocalStatic
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      assert(repository.supportedFormatVersion !== fallbackVersion)
      assert(repository.supportedFormatVersion == softwareVersion)
    }
  }

  "LocalStatic repository" when {
    "saving empty" should {
      "should still include description and root node" in {
        val repository = newRepository
        val output = repository.saveToString(emptyGrid, empty(0), cameras(0))
        val expected = readFile("localStatic/empty.json")
        assert(expected == output)
      }
    }
    "loading empty" should {
      "create new grid with root element" in {
        val repository = newRepository
        val (grid, elements, views) = repository.loadFromStream(
          readStream("localStatic/empty.json"),
          (data, map) => Element(data.storageId, map(data.nodeId), data.position, data),
          (camera, map) => View(camera.storageId, map(camera.nodeId), camera.position, camera)
        )
        assert(1 === grid.flatten.size)
        assertXY(grid.root, 0, 0)
        assert(0 === elements.size)
        assert(0 === views.size)
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
        val repository = newRepository
        val output = repository.saveToString(grid, list(e1), list(c1))
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
        val repository = newRepository
        val output = repository.saveToString(grid, list(e1, e2, e3), list(c1, c2))
        val expected = readFile("localStatic/reusing-nodes.json")
        assert(expected == output)
      }
    }
    "loading with many elements" should {
      "create new grid, representations and cameras" in {
        //      root--c1
        //       |
        //    ___n1___
        //   |       |
        //   n2     n3--c2
        //  :       |
        //  e1     n4--e2
        //  :       :
        //  d1     e3
        val repository = newRepository
        val (grid, elements, views) = repository.loadFromStream(
          readStream("localStatic/reusing-nodes.json"),
          (data, map) => Element(data.storageId, map(data.nodeId), data.position, data),
          (camera, map) => View(camera.storageId, map(camera.nodeId), camera.position, camera)
        )

        assert(7 === grid.flatten.size)
        val n1 = grid.root.children(1)
        val n2 = grid.root.children(0).children(2)
        val n3 = grid.root.children(0).children(1)
        val n4 = grid.root.children(0).children(0).children(0)
        assertXY(n1, 1, -4)
        assertXY(n2, 3, -95)
        assertXY(n3, 99, -67)
        assertXY(n4, 5, -92)

        assert(3 === elements.size)
        val e1 = elements(2)
        val e2 = elements(1)
        val e3 = elements(0)
        assert(Distance(1.2, 3.4, 1.3) === e1.position)
        assert(Distance(0, 0, 1) === e2.position)
        assert(Distance(0, 0, 1) === e3.position)
        assert(n2 === e1.node)
        assert(n4 === e2.node)
        assert(n4 === e3.node)
        assert(Set(("text", "E2")) === e2.source.customFields)
        assert(Set(("text", "E3")) === e3.source.customFields)

        assert(2 === views.size)
        val c1 = views(1)
        val c2 = views(0)
        assert(Distance(0, 0, 1) === c1.position)
        assert(Distance(1, 1.2, 0.987654321) === c2.position)
        assert(grid.root === c1.node)
        assert(n3 === c2.node)
      }
    }
  }

  //
  // Helpers
  //

  private val invalidDistance: AbsoluteDistance = (Node(0, 0), Distance())

  private def emptyGrid = new Grid {
    override private[auginte] def newNode: NodeToNode = (n) => new Node(n.x, n.y) with LinearIds
  }

  private val packageDir = "/" + getClass.getPackage.getName.replaceAll("\\.", "/") + "/"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    LinearIds.reset()
  }

  private def newRepository = new LocalStatic with ConsistentVersion

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

  @throws[Exception]
  private def readStream(name: String, prefix: String = packageDir): InputStream = {
    val strean = getClass.getResourceAsStream(prefix + name)
    val here = File(".").toAbsolute.path
    require(strean != null, s"Resource not found: $prefix$name from $here")
    strean
  }

  trait ZeroPosition extends Data {
    override def node: Node = Node(0, 0)

    override def position: Distance = Distance()
  }

  trait LinearIds extends WithId {
    override val storageId: String = f"${LinearIds.next}%32s".replaceAll(" ", "0")
  }

  trait ConsistentVersion extends LocalStatic {
    override val supportedFormatVersion = Version("0.0.1-FIXTURE")
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

  case class Element(override val storageId: String, node: Node, position: Distance, source: ImportedData) extends Data

  case class View(override val storageId: String, node: Node, position: Distance, source: ImportedCamera) extends Camera
}
