package com.auginte.distribution.repository

import com.auginte.SoftwareVersion
import com.auginte.distribution.data.{Description, Version}
import com.auginte.zooming.Grid
import spray.json._

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

    val data = Map(
      "description" -> description.toJson,
      "elements" -> elements().toJson,
      "cameras" -> cameras().toJson
    )
    val dataJson = data.toJson
    dataJson.prettyPrint
  }

  override def description: Description = Description(softwareVersion, elements().size, cameras().size)

  override def parameters_=(values: List[Symbol]): Unit = _parameters = values

  override def load(): Unit = {
    println(s"Loading ${_parameters}")
  }
}
