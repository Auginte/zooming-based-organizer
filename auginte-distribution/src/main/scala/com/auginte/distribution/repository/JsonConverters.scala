package com.auginte.distribution.repository

import com.auginte.distribution.data.{Camera, Data, Description, Version}
import com.auginte.zooming.Node
import play.api.libs.json._
import play.api.libs.json.Json.toJson


/**
 * Provides implicit values for Data <-> Json conversion
 *
 * Helper for `play-json` library.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object JsonConverters {
  implicit val versionJson = new Writes[Version] {
    override def writes(o: Version): JsValue = toJson(o.version)
  }

  implicit val descriptionJson = new Writes[Description] {
    override def writes(o: Description): JsValue = Json.obj(
      "auginteVersion" -> o.auginteVersion,
      "elements" -> o.elements,
      "cameras" -> o.cameras
    )
  }

  implicit val representationsJson = new Writes[Data] {
    override def writes(o: Data): JsValue = o match {
      case c: Camera => Json.obj("@id" -> s"ca:${c.storageId}")
      case _ =>
        val defaultFields = Seq(
          "@id" -> Json.toJson(s"re:${o.storageId}")
        )
        JsObject(defaultFields ++ o.storageJsonConverter)
    }
  }

  implicit val nodesJson = new Writes[Node] {
    override def writes(o: Node): JsValue = {
      val parent = if (o.parent.isDefined) toJson(s"gn:${o.parent.get.storageId}") else JsNull
      Json.obj(
        "@id" -> s"gn:${o.storageId}",
        "x" -> o.x,
        "y" -> o.y,
        "parent" -> parent
      )
    }
  }
}
