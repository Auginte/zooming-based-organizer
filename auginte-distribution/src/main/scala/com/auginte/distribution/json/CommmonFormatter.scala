package com.auginte.distribution.json

import com.auginte.distribution.data.{Camera, Data, Description, Version}
import com.auginte.zooming.Node
import play.api.libs.json.Json.toJson
import play.api.libs.json._


/**
 * Provides implicit values for Data <-> Json conversion
 *
 * Helper for `play-json` library.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object CommmonFormatter {
  implicit val versionJson = new Writes[Version] {
    override def writes(o: Version): JsValue = toJson(o.version)
  }

  implicit val descriptionJson = new Writes[Description] {
    override def writes(o: Description): JsValue = Json.obj(
      "auginteVersion" -> o.auginteVersion,
      "countElements" -> o.elements,
      "countCameras" -> o.cameras
    )
  }

  implicit val representationJson = new Writes[Data] {
    override def writes(o: Data): JsValue = {
      val id = o match {
        case c: Camera => s"ca:${o.storageId}"
        case r: Data => s"re:${r.storageId}"
      }
      val typeFields = o match {
        case c: Camera => Seq()
        case r: Data => Seq("@type" -> Json.toJson(s"ag:${r.dataType}"))
      }
      val defaultFields = Seq("@id" -> Json.toJson(id))
      JsObject(defaultFields ++ typeFields ++ o.storageJsonConverter ++ o.zoomingJsonConverter)
    }
  }

  implicit val nodesJson = new Writes[Node] {
    override def writes(o: Node): JsValue = {
      val parent = if (o.parent.isDefined) toJson(s"gn:${o.parent.get.storageId}") else JsNull
      Json.obj(
        "@id" -> s"gn:${o.storageId}",
        "posX" -> o.x,
        "posY" -> o.y,
        "parent" -> parent
      )
    }
  }
}
