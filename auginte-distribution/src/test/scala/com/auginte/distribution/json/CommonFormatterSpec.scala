package com.auginte.distribution.json

import com.auginte.distribution.data.{Description, Version}
import com.auginte.test.UnitSpec
import play.api.libs.json.Json

/**
 * Unit test for [[com.auginte.distribution.json.CommonFormatter]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class CommonFormatterSpec extends UnitSpec {
  "Common JSON Formatter" should {
    "provide implicits" when {
      "converting from description" in {
        import com.auginte.distribution.json.CommonFormatter._
        assert(descriptionText1 == Json.stringify(Json.toJson(descriptionData1)))
        assert(descriptionText2 == Json.stringify(Json.toJson(descriptionData2)))
      }
      "converting to description" in {
        import com.auginte.distribution.json.CommonFormatter._
        assert(descriptionData1 == Json.parse(descriptionText1).as[Description])
        assert(descriptionData2 == Json.parse(descriptionText2).as[Description])
      }
    }
  }

  // Test data

  val descriptionData1 = Description(Version("1.2.3-SNAPSHOT"), 5, 4)
  val descriptionText1 = """{"auginteVersion":"1.2.3-SNAPSHOT","countElements":5,"countCameras":4}"""
  val descriptionData2 = Description(Version("0.1.3"), 0, 15)
  val descriptionText2 = """{"auginteVersion":"0.1.3","countElements":0,"countCameras":15}"""
}
