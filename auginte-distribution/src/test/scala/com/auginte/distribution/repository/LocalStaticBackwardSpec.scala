package com.auginte.distribution.repository

import java.io.InputStream

import com.auginte.common.{SoftwareVersion, WithId}
import com.auginte.distribution.data._
import com.auginte.distribution.exceptions.{UnconnectedIds, UnsupportedElement, UnsupportedStructure, UnsupportedVersion}
import com.auginte.test.UnitSpec
import com.auginte.transforamtion.Relation
import com.auginte.zooming._

import scala.io.Source
import scala.reflect.io.File

/**
 * Backward compatibility tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticBackwardSpec extends UnitSpec with NodeAssertions {
  "LocalStatic object" should {
    "read v0.6.2 files (wihotu sources)" in {
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
        readStream("localStatic/v0_6_2/reusing-nodes.json"),
        (data, map) => Some(Element(data.storageId, map(data.nodeId), data.position, data)),
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
      val e1 = elements(0).get
      val e2 = elements(2).get
      val e3 = elements(1).get
      assert(Coordinates(1.2, 3.4, 1.3) === e1.position)
      assert(Coordinates(0, 0, 1) === e2.position)
      assert(Coordinates(0, 0, 1) === e3.position)
      assert(n2 === e1.node)
      assert(n4 === e2.node)
      assert(n4 === e3.node)
      assert(Map("text" -> "E2") === e2.source.customFields)
      assert(Map("text" -> "E3") === e3.source.customFields)

      assert(2 === views.size)
      val c1 = views(1)
      val c2 = views(0)
      assert(Coordinates(0, 0, 1) === c1.position)
      assert(Coordinates(1, 1.2, 0.987654321) === c2.position)
      assert(grid.root === c1.node)
      assert(n3 === c2.node)
    }
  }

  //
  // Helpers
  //

  private val invalidDistance: GlobalCoordinates = (Node(0, 0), Coordinates())

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
    val stream = getClass.getResourceAsStream(prefix + name)
    val here = File(".").toAbsolute.path
    require(stream != null, s"Resource not found: $prefix$name from $here")
    stream
  }

  private val elementCreator = (data: ImportedData, map: IdToRealNode) =>
    new Element(data.storageId, safeMap(map, data.storageId, data.nodeId), data.position, data) {
      sources = data.sources
    }

  private val cameraCreator = (camera: ImportedCamera, map: IdToRealNode) =>
    View(camera.storageId, safeMap(map, camera.storageId, camera.nodeId), camera.position, camera)

  @throws[UnconnectedIds]
  private def safeMap(map: IdToRealNode, from: String, to: String): Node = if (map.contains(to)) {
    map(to)
  } else {
    throw UnconnectedIds(from, to)
  }

  trait ZeroPosition extends Data {
    override def node: Node = Node(0, 0)

    override def position: Coordinates = Coordinates()
  }

  trait LinearIds extends WithId {
    override val storageId: String = f"${LinearIds.next}%32s".replaceAll(" ", "0")
  }

  trait ConsistentVersion extends LocalStatic {
    override val supportedFormatVersion = Version("0.0.1-FIXTURE")
  }

  case class ZoomableElement(node: Node, position: Coordinates = Coordinates()) extends Data with LinearIds

  case class ZoomableText(node: Node, position: Coordinates = Coordinates(), override val text: String) extends Text with LinearIds

  case class ZoomableCamera(node: Node, position: Coordinates = Coordinates()) extends Camera with LinearIds

  object LinearIds {
    var id = 0

    def next = {
      id = id + 1
      id
    }

    def reset(): Unit = id = 1
  }

  case class Element(override val storageId: String, node: Node, position: Coordinates, source: ImportedData) extends Data {
    sources = source.sources
  }

  case class View(override val storageId: String, node: Node, position: Coordinates, source: ImportedCamera) extends Camera
}
