package com.auginte.distribution.json

import com.auginte.distribution.data._
import com.auginte.distribution.repository.CustomFields
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

  implicit val representationFormatter = new Format[Data] {
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
      JsObject(
        defaultFields
          ++ typeFields
          ++ o.storageJsonConverter
          ++ o.zoomingJsonConverter
          ++ o.transformingJsonConverter
      )
    }

    override def reads(json: JsValue): JsResult[Data] = {
      val id = (json \ "@id").asOpt[String]
      val typeName = (json \ "@type").asOpt[String]
      val x = (json \ "x").asOpt[Double]
      val y = (json \ "y").asOpt[Double]
      val scale = (json \ "scale").asOpt[Double]
      val node = (json \ "node").asOpt[String]
      val customFields = json match {
        case o: JsObject if typeName.isDefined =>
          val typePrefix = s"${typeName.get}/"
          val isCustomField: ((String, Any)) => Boolean = pair => pair._1.startsWith(typePrefix)
          val suffix = (s: String) => s.substring(typePrefix.length)
          o.fieldSet.filter(isCustomField).map(pair => suffix(pair._1) -> pair._2.as[String]).toMap
        case _ => Map[String, String]()
      }
      if (isDefined(id, typeName, x, y, scale, node)) {
        val result = ImportedData(id.get, typeName.get, x.get, y.get, scale.get, node.get, customFields)
        new JsSuccess[ImportedData](result)
      } else {
        JsError(s"Not valid representation: id=$id, type=$typeName, x=$x, y=$y, scale=$scale, node=$node customFields=$customFields")
      }
    }
  }

  implicit val cameraReads = new Reads[Camera] {
    override def reads(json: JsValue): JsResult[Camera] = {
      val id = (json\ "@id").asOpt[String]
      val x = (json\ "x").asOpt[Double]
      val y = (json\ "y").asOpt[Double]
      val scale = (json\ "scale").asOpt[Double]
      val node = (json\ "node").asOpt[String]
      if (isDefined(id, x, y, scale, node)) {
        val result = ImportedCamera(id.get, x.get, y.get, scale.get, node.get)
        new JsSuccess[ImportedCamera](result)
      } else {
        JsError(s"Not valid camera: id=$id, x=$x, y=$y, scale=$scale, node=$node")
      }
    }
  }

  implicit val nodesWrites = new Writes[Node] {
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

  implicit val nodesReads = new Reads[Node] {
    override def reads(json: JsValue): JsResult[Node] = {
      val id = (json \ "@id").asOpt[String]
      val x = (json \ "posX").asOpt[Int]
      val y = (json \ "posY").asOpt[Int]
      val parent = (json \ "parent").asOpt[String].getOrElse("")
      if (isDefined(x, y, id)) {
        val result = new ImportedNode(x.get, y.get, id.get, parent)
        JsSuccess[ImportedNode](result)
      } else {
        JsError(s"Not valid node: id=$id, x=$x, y=$y, parent=$parent")
      }
    }
  }

  private def isDefined(xs: Option[Any]*): Boolean = xs.forall(_.isDefined)
}
