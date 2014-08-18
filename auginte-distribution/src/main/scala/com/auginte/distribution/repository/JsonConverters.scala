package com.auginte.distribution.repository

import com.auginte.distribution.data.{Camera, Data, Description, Version}
import play.api.libs.json._


/**
 * Provides implicit values for Data <-> Json conversion
 *
 * Helper for `play-json` library.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object JsonConverters {
  implicit val versionJson = new Writes[Version] {
    override def writes(o: Version): JsValue = Json.toJson(o.version)
  }

  implicit val descriptionJson = new Writes[Description] {
    override def writes(o: Description): JsValue = Json.obj(
      "auginteVersion" -> o.auginteVersion,
      "elements" -> o.elements,
      "cameras" -> o.cameras
    )
  }

  implicit val elementsJson = new Writes[Data] {
    override def writes(o: Data): JsValue = {
      val defaultFields = Seq(
        "storageId" -> Json.toJson(o.storageId)
      )
      JsObject(defaultFields ++ o.storageJsonConverter)
    }
  }

  implicit val camerasJson = new Writes[Camera] {
    override def writes(o: Camera): JsValue = Json.obj(
      o.storageId -> o.getClass.getName
    )
  }
}
