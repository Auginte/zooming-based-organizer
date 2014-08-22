package com.auginte.distribution.repository

import com.auginte.common.SoftwareVersion
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
      val repository = new LocalStatic(emptyGrid, fakeElements(0), fakeCameras(0), converter)
      val description = repository.description
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      assert(description.auginteVersion !== fallbackVersion)
      assert(description.auginteVersion == softwareVersion)
    }
    "include approximated number of elements" in {
      val r1 = new LocalStatic(emptyGrid, fakeElements(0), fakeCameras(0), converter)
      val r2 = new LocalStatic(emptyGrid, fakeElements(1), fakeCameras(1), converter)
      val r3 = new LocalStatic(emptyGrid, fakeElements(5), fakeCameras(0), converter)
      val r4 = new LocalStatic(emptyGrid, fakeElements(0), fakeCameras(6), converter)
      val r5 = new LocalStatic(emptyGrid, fakeElements(125), fakeCameras(63), converter)
      val r6 = new LocalStatic(emptyGrid, fakeElements(190), fakeCameras(100), converter)
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
        val repository = new LocalStatic(emptyGrid, linearElements(0), cameras(0), converter) with ConsistentVersion
        val output = repository.saveToString
        val expected = readFile("localStatic/empty.json")
        assert(expected == output)
      }
    }
    "saving multiple nodes" should {
      "save ids with prefixes" in {
        val grid = emptyGrid
        val n1 = grid.getNode(grid.root, 1.23, -4.56, 0.01)
        val n2 = grid.getNode(n1, 3.23, 5.56, 0.01)
        val e1 = ZoomableElement(n1)
        val c1 = ZoomableCamera(grid.root)
        val repository = new LocalStatic(grid, list(e1), list(c1), converter) with ConsistentVersion
        val output = repository.saveToString
        val expected = readFile("localStatic/multiple-nodes.json")
        assert(expected == output)
      }
    }
    "saving with many elements" should {
      "representations should reuse duplicated data and nodes" in {

      }
    }
  }


  //
  // Helpers
  //

  private val invalidDistance: AbsoluteDistance = (Node(0, 0), Distance())

  private val converter: Converter = (d: Data) => d match {
    case z: ZoomableElement => Some((z.node, z.distance))
    case z: ZoomableCamera => Some((z.node, z.distance))
    case _ => Some(invalidDistance)
  }


  private val emptyGrid = new Grid {
    override private[auginte] def newNode: NodeToNode = (n) => new Node(n.x, n.y) with LinearIds
  }
  private val packageDir = "/" + getClass.getPackage.getName.replaceAll("\\.", "/") + "/"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    LinearIds.reset()
  }

  private def fakeElements(count: Int): Elements = () => for (i <- 1 to count) yield new Data {}

  private def linearElements(count: Int): Elements = () => for (i <- 1 to count) yield new Data with LinearIds {}

  private def texts(count: Int): Elements = () => for (i <- 1 to count) yield new Text with LinearIds {
    text = i.toString
  }

  private def fakeCameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera {}

  private def cameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera with LinearIds {}

  private def list[A](xs: A*) = () => xs.toList

  @throws[Exception]
  private def readFile(name: String, prefix: String = packageDir): String = {
    val resource = getClass.getResource(prefix + name)
    val here = File(".").toAbsolute.path
    require(resource != null, s"Resource not found: $prefix$name from $here")
    Source.fromURL(resource).getLines() mkString "\n"
  }

  trait LinearIds extends Data {
    override val storageId: String = f"${LinearIds.next}%32s".replaceAll(" ", "0")
  }

  trait ConsistentVersion extends LocalStatic {
    override def description: Description = Description(
      Version("0.0.1-FIXTURE"), super.description.elements, super.description.cameras
    )
  }

  case class ZoomableElement(node: Node, distance: Distance = Distance()) extends Data with LinearIds

  case class ZoomableCamera(node: Node, distance: Distance = Distance()) extends Camera with LinearIds

  object LinearIds {
    var id = 0

    def next = {
      id = id + 1
      id
    }

    def reset(): Unit = id = 1
  }

}
