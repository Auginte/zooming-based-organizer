package com.auginte.distribution.json

import com.auginte.distribution.data.{Camera, Data, Description, Version}
import com.auginte.zooming.Node
import play.api.libs.json._
import play.api.libs.json.Json.toJson
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Provides implicit values for Data <-> Json conversion
 *
 * Helper for `play-json` library.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object CommonFormatter {
  implicit val versionJson = new Format[Version] {
    override def writes(o: Version): JsValue = toJson(o.version)

    override def reads(json: JsValue): JsResult[Version] = JsSuccess(new Version(json.as[String]))
  }

  implicit val descriptionWrites = (
    (__ \ "auginteVersion").write[Version] and
      (__ \ "countElements").write[Int] and
      (__ \ "countCameras").write[Int]
    )(unlift(Description.unapply))

  implicit val descriptionReads = (
    (__ \ "auginteVersion").read[Version] and
      (__ \ "countElements").read[Int] and
      (__ \ "countCameras").read[Int]
    )(Description)

  implicit val representationWrites = new Writes[Data] {
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
