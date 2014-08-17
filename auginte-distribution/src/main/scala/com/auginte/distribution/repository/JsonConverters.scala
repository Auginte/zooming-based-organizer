package com.auginte.distribution.repository

import com.auginte.distribution.data.{Camera, Data, Description, Version}
import spray.json._

/**
 * Provides implicit values for Data <-> Json conversion
 *
 * Helper for `spray-json` library.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object JsonConverters extends DefaultJsonProtocol {
  implicit val versionFormatter = jsonFormat(Version, "version")

  implicit val descriptionFormatter = jsonFormat(Description, "version", "elements", "cameras")

  implicit object DataJsonFormat extends RootJsonFormat[Data] {
    def write(d: Data) = JsObject("id" -> JsString(d.storageId))

    def read(value: JsValue) = {
      value.asJsObject.getFields("id") match {
        case Seq(JsString(id)) => Data(id)
        case _ => throw new DeserializationException("Data expected")
      }
    }
  }

  implicit object CameraJsonFormat extends RootJsonFormat[Camera] {
    def write(c: Camera) = JsObject("id" -> JsString(c.storageId))

    def read(value: JsValue) = {
      value.asJsObject.getFields("id") match {
        case Seq(JsString(id)) => Camera(id)
        case _ => throw new DeserializationException("Camera expected")
      }
    }
  }

}
