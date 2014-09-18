package com.auginte.transformation

import com.auginte.test.UnitSpec
import com.auginte.transforamtion.Transformable

/**
 * unit tests for [[com.auginte.transforamtion.Transformable]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class TransformableSpec extends UnitSpec with TransformableSpecHelpers {
  "Transformable object" when {
    "transforming to similar type" should {
      "copy old content" should {
        "safe reference to source" in {
          // data1 -> transformed1 -> transformed2 -> transformed3
          val data1 = TransformableMock("A")
          val data2 = TransformableMock("B")
          val transformed1 = data1.transformed()
          val transformed2 = transformed1.transformed()
          val transformed3 = transformed2.transformed()
          assert(data1 === transformed1.sources.head.target)
          assert(transformed1 === transformed2.sources.head.target)
          assert(transformed2 === transformed3.sources.head.target)
          assert(data2 !== transformed3.sources.head.target)
          assert(data2 !== transformed2.sources.head.target)
          assert(!(data1 eq transformed3.sources.head.target))
          assert(!(data1 eq transformed2.sources.head.target))
          assert(0 === data1.sources.size)
          assert(0 === data2.sources.size)
          for (element <- List(transformed1, transformed2, transformed3)) {
            assert(1 === element.sources.size)
            assert(0 === element.sources.head.parameters.size)
          }
        }
        "safe parameters of reference" in {
          val data1 = TransformableMock("A")
          val transformed1 = data1.transformed(Map("type" -> "B"))
          val transformed2 = transformed1.transformed(Map("type" -> "C", "param" -> "D"))
          assert(data1 === transformed1.sources.head.target)
          assert(transformed1 === transformed2.sources.head.target)
          assert(Map("type" -> "B") === transformed1.sources.head.parameters)
          assert(Map("type" -> "C", "param" -> "D") === transformed2.sources.head.parameters)
        }
      }
    }
    "converting to another element" should {
      "safe reference to source" in pending
      "safe parameters of reference" in pending
    }
    "using multiple sources" should {
      "safe reference to source" in pending
      "safe parameters of reference" in pending
    }
  }
}
sealed trait TransformableSpecHelpers {
  case class TransformableMock(data: String) extends Transformable {
     override protected def copy: Transformable = new TransformableMock(data)
  }
}
