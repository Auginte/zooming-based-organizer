package com.auginte.distribution.json

import com.auginte.distribution.data._
import com.auginte.test.UnitSpec
import play.api.libs.json.Json

/**
 * Unit test for [[com.auginte.distribution.json.BigJson]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class BigJsonSpec extends UnitSpec {

  "BigJson" should {
    "parse json in chunks, producing events" in {
      import KeysToJsValues.localStorage
      var nodes: List[ImportedNode] = List()
      var representations: List[ImportedData] = List()
      var cameras:List[ImportedCamera] = List()
      val stream = getClass.getResourceAsStream("/com/auginte/distribution/repository/localStatic/reusing-nodes.json")

      fixture.read(stream, event => {
        if (localStorage.contains(event.tagName)) {
          val decoded = localStorage(event.tagName)(event.rawValue)
          event.tagName match {
            case "@context" =>
              assert("http://auginte.com/ns/v0.6/localStatic.jsonld" === decoded.asInstanceOf[String])
            case "description" =>
              assert(Description(Version("0.0.1-FIXTURE"),3,2) === decoded.asInstanceOf[Description])
            case "nodes" => nodes = decoded.asInstanceOf[ImportedNode] :: nodes
            case "representations" => representations = decoded.asInstanceOf[ImportedData] :: representations
            case "cameras" => cameras = decoded.asInstanceOf[ImportedCamera] :: cameras
          }
        } else fail(s"Unknown field: $event")
        true
      })

      val expectedNodes = List(
        new ImportedNode(5, -92, "gn:00000000000000000000000000000004", "gn:00000000000000000000000000000003"),
        new ImportedNode(3, -95, "gn:00000000000000000000000000000004", "gn:00000000000000000000000000000003"),
        new ImportedNode(99, -67, "gn:00000000000000000000000000000002", "gn:00000000000000000000000000000003"),
        new ImportedNode(99, -66, "gn:00000000000000000000000000000003", "gn:00000000000000000000000000000003"),
        new ImportedNode(1, -4, "gn:00000000000000000000000000000002", "gn:00000000000000000000000000000001"),
        new ImportedNode(1, -3, "gn:00000000000000000000000000000003", "gn:00000000000000000000000000000001"),
        new ImportedNode(0, 0, "gn:00000000000000000000000000000001", "")
      )
      assert(expectedNodes === nodes)
      val expectedRepresentations = List(
        new ImportedData("re:00000000000000000000000000000007","ag:Text",0.0,0.0,1.0,"gn:00000000000000000000000000000004",Set(("text","E3"))),
        new ImportedData("re:00000000000000000000000000000006","ag:Text",0.0,0.0,1.0,"gn:00000000000000000000000000000004",Set(("text","E2"))),
        new ImportedData("re:00000000000000000000000000000005","ag:Abstract",1.2,3.4,1.3,"gn:00000000000000000000000000000004",Set())
      )
      assert(expectedRepresentations === representations)
      val expectedCameras = List(
        new ImportedCamera("ca:00000000000000000000000000000009",1.0,1.2,0.987654321,"gn:00000000000000000000000000000002"),
        new ImportedCamera("ca:00000000000000000000000000000008",0.0,0.0,1.0,"gn:00000000000000000000000000000001")
      )
      assert(expectedCameras === cameras)
    }
    "have functionality to cancel parsing in the middle" in {
      val stream = getClass.getResourceAsStream("/com/auginte/distribution/repository/localStatic/reusing-nodes.json")
      var tagNames: Set[String] = Set()
      fixture.read(stream, event => {
        tagNames = tagNames + event.tagName
        event.tagName match {
          case "@context" => true
          case "description" => true
          case "nodes" => false
          case "representations" =>
            fail(s"Continued after cancellation: $tagNames")
            false
        }
      })
    }
  }

  def fixture = new BigJson

}
