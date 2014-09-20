package com.auginte.distribution.json

import com.auginte.transforamtion.{External, Descendant}
import play.api.libs.json.{JsObject, JsArray, Json, JsValue}

/**
 * JSON converters for [[Descendant]] elements.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait TransformableFormatter extends Descendant {
  def transformingJsonConverter: Seq[(String, JsValue)] = List(
    "sources" -> Json.toJson(sources.map(relation => Json.obj(
      "target" -> s"re:${relation.target.storageId}",
      "parameters" -> JsObject(relation.parameters.map(p => s"agt:${p._1}" -> Json.toJson({p._2})).toList)
    )))
  )
}
