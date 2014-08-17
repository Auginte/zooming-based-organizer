package com.auginte.distribution.repository

import com.auginte.SoftwareVersion
import com.auginte.test.UnitSpec
import com.auginte.zooming.Grid
import com.auginte.distribution.data.Version

/**
 * Unit tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticSpec extends UnitSpec {
  "LocalStatic repository" when {
    "in description section" should {
      val repository = emptyLocalStatic
      val description = repository.description
      val softwareVersion = Version(SoftwareVersion.toString)
      val fallbackVersion = Version(SoftwareVersion.fallBackVersion)
      "include software version number" in {
        assert(description.version !== fallbackVersion)
        assert(description.version == softwareVersion)
      }
      "include boundaries" in {

      }
      "include approximate number of elements" in {

      }
    }
  }

  private def gridStub = new Grid {}

  private def emptyLocalStatic = new LocalStatic(gridStub, () => List(), () => List(), (d) => None)
}
