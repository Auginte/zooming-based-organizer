package com.auginte.distribution.data

import com.auginte.test.UnitSpec

/**
 * Unit test for [[com.auginte.distribution.data.Data]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class DataSpec extends UnitSpec {

  "Data object" must {
    "have ids of 32 chars length" in {
      for (i <- 0 to 100) {
        val data = new Data{}
        assert(32 === data.storageId.length)
      }
    }
    "generate have unique ids" in {
      val amount = 100
      val all = new Array[String](amount)
      for (i <- 1 to amount) {
        val data = new Data{}
        all(i - 1) = data.storageId
      }
      assert(amount === all.toSet.size)
    }
  }
}
