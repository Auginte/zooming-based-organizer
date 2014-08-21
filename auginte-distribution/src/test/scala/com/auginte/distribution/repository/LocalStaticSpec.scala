package com.auginte.distribution.repository

import com.auginte.common.SoftwareVersion
import com.auginte.distribution.data._
import com.auginte.test.UnitSpec
import com.auginte.zooming.{Grid, Distance, Node, AbsoluteDistance}

/**
 * Unit tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticSpec extends UnitSpec {
  "LocalStatic repository" should {
    "include software version number" in {
      val repository = new LocalStatic(grid, fakeElements(0), fakeCameras(0), converter)
      val description = repository.description
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      assert(description.auginteVersion !== fallbackVersion)
      assert(description.auginteVersion == softwareVersion)
    }
    "include approximated number of elements" in {
      val r1 = new LocalStatic(grid, fakeElements(0), fakeCameras(0), converter)
      val r2 = new LocalStatic(grid, fakeElements(1), fakeCameras(1), converter)
      val r3 = new LocalStatic(grid, fakeElements(5), fakeCameras(0), converter)
      val r4 = new LocalStatic(grid, fakeElements(0), fakeCameras(6), converter)
      val r5 = new LocalStatic(grid, fakeElements(125), fakeCameras(63), converter)
      val r6 = new LocalStatic(grid, fakeElements(190), fakeCameras(100), converter)
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
    "provide data in JSON format" in {
      LinearIds.reset()
      val r1 = new LocalStatic(grid, linearElements(0), cameras(0), converter) with ConsistentVersion
      val t1 = r1.saveToString
      val r2 = new LocalStatic(grid, linearElements(1), cameras(1), converter) with ConsistentVersion
      val t2 = r2.saveToString
      val r3 = new LocalStatic(grid, texts(1), cameras(1), converter) with ConsistentVersion
      val t3 = r3.saveToString
      val r4 = new LocalStatic(grid, texts(2), cameras(3), converter) with ConsistentVersion
      val t4 = r4.saveToString
      val e1 = """{"description":{"auginteVersion":"0.0.1-SNAPSHOT","elements":0,"cameras":0},"elements":[],"cameras":[]}"""
      val e2 = """{"description":{"auginteVersion":"0.0.1-SNAPSHOT","elements":1,"cameras":1},"elements":[{"storageId":"00000000000000000000000000000006"}],"cameras":[{"storageId":"00000000000000000000000000000007"}]}"""
      val e3 = """{"description":{"auginteVersion":"0.0.1-SNAPSHOT","elements":1,"cameras":1},"elements":[{"storageId":"00000000000000000000000000000012","text":"1"}],"cameras":[{"storageId":"00000000000000000000000000000013"}]}"""
      val e4 = """{"description":{"auginteVersion":"0.0.1-SNAPSHOT","elements":2,"cameras":3},"elements":[{"storageId":"00000000000000000000000000000024","text":"1"},{"storageId":"00000000000000000000000000000025","text":"2"}],"cameras":[{"storageId":"00000000000000000000000000000026"},{"storageId":"00000000000000000000000000000027"},{"storageId":"00000000000000000000000000000028"}]}"""
      assert(e1 === t1)
      assert(e2 === t2)
      assert(e3 === t3)
      assert(e4 === t4)
    }
  }

  private val invalidDistance: AbsoluteDistance = (Node(0, 0), Distance())

  private val converter: Converter = (d: Data) => Some(invalidDistance)

  private val grid = new Grid {}

  private def fakeElements(count: Int): Elements = () => for (i <- 1 to count) yield new Data {}

  private def linearElements(count: Int): Elements = () => for (i <- 1 to count) yield new Data with LinearIds {}

  private def texts(count: Int): Elements = () => for (i <- 1 to count) yield new Text with LinearIds {
    text = i.toString
  }

  private def fakeCameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera {}

  private def cameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera with LinearIds {}

  trait LinearIds extends Data {
    override val storageId: String = f"${LinearIds.next}%32s".replaceAll(" ", "0")
  }

  object LinearIds {
    var id = 1

    def next = {
      id = id + 1
      id
    }

    def reset(): Unit = id = 1
  }

  trait ConsistentVersion extends LocalStatic {
    override def description: Description = Description(
      Version("0.0.1-SNAPSHOT"), super.description.elements, super.description.cameras
    )
  }
}
