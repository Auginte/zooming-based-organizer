package com.auginte.distribution.json

import com.auginte.distribution.data.{Camera, Data, Description}
import com.auginte.zooming.Node
import play.api.libs.json.Json


/**
 * Provides mapping for [[BigJson]] to [[play.api.libs.json]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object KeysToJsValues {
  import CommonFormatter._

  val localStorage = Map(
    "@context" -> ((d: String) => d),
    "description" -> ((d: String) => Json.parse(d).as[Description]),
    "nodes" -> ((d: String) => Json.parse(d).as[Node]),
    "representations" -> ((d: String) => Json.parse(d).as[Data]),
    "cameras" -> ((d: String) => Json.parse(d).as[Camera])
  )
}
