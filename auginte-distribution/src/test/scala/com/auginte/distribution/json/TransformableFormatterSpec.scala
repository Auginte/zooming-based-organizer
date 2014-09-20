package com.auginte.distribution.json

import com.auginte.test.UnitSpec
import com.auginte.transforamtion.{Descendant, Relation}
import play.api.libs.json.{JsObject, Json}

/**
 * Unit tests for [[TransformableFormatter]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class TransformableFormatterSpec extends UnitSpec with TransformableFormatterSpecHelpers {
  "Transformable formatter" should {
    "provide empty array for no sources" in {
      val data1 = new TransformableFormatter {}
      val json1 = JsObject(data1.transformingJsonConverter)
      assert("""{"sources":[]}""" === json1.toString())
    }
    "provide array of source ids including relation context" in {
      val data1 = data("1")
      val data2 = data("2")
      val data3 = data("3")
      val data4 = new TransformableFormatter {
        sources = List(
          Relation(data1),
          Relation(data2, Map("type" -> "cloned")),
          Relation(data3, Map("type" -> "cloned", "date" -> "now"))
        )
      }
      val json4 = JsObject(data4.transformingJsonConverter)
      val expected1 = """{"sources":[{"target":"re:1","parameters":{}},{"target":"re:2","parameters":{"agt:type":"cloned"}},{"target":"re:3","parameters":{"agt:type":"cloned","agt:date":"now"}}]}"""
      assert(expected1 === json4.toString())
    }
  }
}
sealed trait TransformableFormatterSpecHelpers {
  def data(id: String) = new Descendant {
    override val storageId = id
  }
}