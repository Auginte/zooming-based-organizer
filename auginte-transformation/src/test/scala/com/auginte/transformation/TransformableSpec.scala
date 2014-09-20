package com.auginte.transformation

import com.auginte.test.UnitSpec
import com.auginte.transforamtion.{Relation, Transformable}

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
          val data1 = TransformableString("A")
          val data2 = TransformableString("B")
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
          val data1 = TransformableString("A")
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
      "safe reference to source" in {
        val data1 = TransformableString("A")
        val transformed1 = data1.transformedTo(new TransformableInt(1))
        val transformed2 = transformed1.transformedTo(new TransformableString("B"))
        assert(data1 === transformed1.sources.head.target)
        assert(transformed1 === transformed2.sources.head.target)
        assert(data1.isInstanceOf[TransformableString])
        assert(transformed1.isInstanceOf[TransformableInt])
        assert(transformed2.isInstanceOf[TransformableString])
      }
      "safe parameters of reference" in {
        val data1 = TransformableString("A")
        val transformed1 = data1.transformedTo(new TransformableInt(1), Map("C" -> "D"))
        val transformed2 = transformed1.transformedTo(new TransformableString("B"), Map("E" -> "F", "G" -> "H"))
        assert(data1 === transformed1.sources.head.target)
        assert(transformed1 === transformed2.sources.head.target)
        assert(data1.isInstanceOf[TransformableString])
        assert(transformed1.isInstanceOf[TransformableInt])
        assert(transformed2.isInstanceOf[TransformableString])
        assert(Map("C" -> "D") === transformed1.sources.head.parameters)
        assert(Map("E" -> "F", "G" -> "H") === transformed2.sources.head.parameters)
      }
    }
    "using multiple sources" should {
      "use last source as fastest to read" in {
        val data1 = new TransformableString("Test")
        val data2 = data1.transformed(Map("added" -> "fist"))
        data2.sources = Relation(data1, Map("added" -> "last")) :: data2.sources
        assert(2 === data2.sources.size)
        assert(Map("added" -> "last") === data2.sources.head.parameters)
        assert(Map("added" -> "fist") === data2.sources.tail.head.parameters)
      }
    }
    "using multi-directional transformation" should {
      "be able to swap sources" in {
        val data1 = TransformableString("A")
        val transformed1 = data1.transformed()
        assert(0 === data1.sources.size)
        assert(data1 === transformed1.sources.head.target)
        assert(data1 eq data1.original)
        assert(transformed1 eq transformed1.original)
        transformed1.swapSources(data1)
        assert(transformed1 === data1.sources.head.target)
        assert(0 === transformed1.sources.size)
      }
    }
  }
}
sealed trait TransformableSpecHelpers {
  case class TransformableString(data: String) extends Transformable[TransformableString] {
     override protected def copy = new TransformableString(data)
  }

  case class TransformableInt(data: Int) extends Transformable[TransformableInt] {
    override protected def copy = new TransformableInt(data)
  }
}
