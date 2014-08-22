package com.auginte.distribution.repository

import com.auginte.common.SoftwareVersion
import com.auginte.distribution.data.{Description, Version}
import com.auginte.zooming.Grid
import play.api.libs.json.Json

/**
 * Simples repository saving everything to JSON files
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStatic(grid: Grid, elements: Elements, cameras: Cameras, converter: Converter) extends Repository {

  private val softwareVersion = Version(SoftwareVersion.toString)

  private var _parameters: List[Symbol] = List()

  override def save(): Unit = {
    println(s"Saving ${_parameters} ${elements()} ${cameras()}")
  }

  def saveToString: String = {

    import com.auginte.distribution.repository.JsonConverters._

    val data = Json.toJson(
      Map(
        "description" -> Json.toJson(description),
        "nodes" -> Json.toJson(nodes),
        "elements" -> Json.toJson(elements()),
        "cameras" -> Json.toJson(cameras())
      )
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
