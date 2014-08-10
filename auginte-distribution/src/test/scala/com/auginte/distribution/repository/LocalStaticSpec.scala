package com.auginte.distribution.repository

import com.auginte.Version
import com.auginte.test.UnitSpec
import com.auginte.zooming.Grid

/**
 * Unit tests for [[com.auginte.distribution.repository.LocalStatic]] repository.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStaticSpec extends UnitSpec {
  "LocalStatic repository" when {
    "saving" should {
      "include version number" in {
        val repository = new LocalStatic(gridStub, () => List(), () => List(), (d) => None)
        assert(repository.getVersion !== Version.fallBackVersion)
        val numeric = Version.toDouble(repository.getVersion)
        assert(numeric >= 0.006001)
      }
    }
  }

  private def gridStub = new Grid {}


}
