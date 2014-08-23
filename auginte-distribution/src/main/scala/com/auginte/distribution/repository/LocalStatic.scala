package com.auginte.distribution.repository

import com.auginte.common.SoftwareVersion
import com.auginte.distribution.data.{Description, Version}
import com.auginte.distribution.json.CommonFormatter
import com.auginte.zooming.Grid
import play.api.libs.json.Json

/**
 * Simples repository saving everything to JSON files
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStatic(grid: Grid, elements: Elements, cameras: Cameras) extends Repository {

  private val softwareVersion = Version(SoftwareVersion.toString)

  private var _parameters: List[Symbol] = List()

  override def save(): Unit = {
    println(s"Saving ${_parameters} ${elements()} ${cameras()}")
  }

  def saveToString: String = {

    import CommonFormatter._

    val data = Json.obj(
      "@context" -> Json.toJson("http://auginte.com/ns/v0.6/localStatic.jsonld"),
      "description" -> Json.toJson(description),
      "nodes" -> Json.toJson(nodes),
      "representations" -> Json.toJson(elements()),
      "cameras" -> Json.toJson(cameras())
    )
    Json.stringify(data)
  }

  override def description: Description = Description(softwareVersion, elements().size, cameras().size)

  override def parameters_=(values: List[Symbol]): Unit = _parameters = values

  override def load(): Unit = {
    println(s"Loading ${_parameters}")
  }

  private def nodes = grid.flatten
}
