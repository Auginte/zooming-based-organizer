package com.auginte.distribution.data

import com.auginte.test.UnitSpec
import com.auginte.zooming.{Node, Coordinates}

/**
 * Unit test for [[com.auginte.distribution.data.Data]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class DataSpec extends UnitSpec {

  "Data object" must {
    "have ids of 32 chars length" in {
      for (i <- 0 to 100) {
        val data = dataFixture
        assert(32 === data.storageId.length)
      }
    }
    "generate have unique ids" in {
      val amount = 100
      val all = new Array[String](amount)
      for (i <- 1 to amount) {
        val data = dataFixture
        all(i - 1) = data.storageId
      }
      assert(amount === all.toSet.size)
    }
  }
  
  def dataFixture = new Data {
    override def node: Node = Node(0, 0)
    override def position: Coordinates = Coordinates()
  }
}
