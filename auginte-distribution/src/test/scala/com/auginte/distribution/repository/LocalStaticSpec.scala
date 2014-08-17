package com.auginte.distribution.repository

import com.auginte.SoftwareVersion
import com.auginte.distribution.data.{Camera, Data, Version}
import com.auginte.test.UnitSpec
import com.auginte.zooming.{Grid, AbsoluteDistance, Distance, Node}

/**
 * Unit tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticSpec extends UnitSpec {
  "LocalStatic repository" should {
    "include software version number" in {
      val repository = new LocalStatic(grid, elements(0), cameras(0), converter)
      val description = repository.description
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      assert(description.version !== fallbackVersion)
      assert(description.version == softwareVersion)
    }
    "include approximated number of elements" in {
      val r1 = new LocalStatic(grid, elements(0), cameras(0), converter)
      val r2 = new LocalStatic(grid, elements(1), cameras(1), converter)
      val r3 = new LocalStatic(grid, elements(5), cameras(0), converter)
      val r4 = new LocalStatic(grid, elements(0), cameras(6), converter)
      val r5 = new LocalStatic(grid, elements(125), cameras(63), converter)
      val r6 = new LocalStatic(grid, elements(190), cameras(100), converter)
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
    "be convertable to JSON string" in {
      val r2 = new LocalStatic(grid, elements(1), cameras(1), converter)
      println(r2.saveToString)
    }
  }

  private val invalidDistance: AbsoluteDistance = (Node(0, 0), Distance())

  private val converter: Converter = (d: Data) => Some(invalidDistance)

  private val grid = new Grid {}

  private def elements(count: Int): Elements = () => for (i <- 1 to count) yield new Data {}

  private def cameras(count: Int): Cameras = () => for (i <- 1 to count) yield new Camera {}
}
