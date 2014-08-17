package com.auginte.distribution.repository

import com.auginte.SoftwareVersion
import com.auginte.distribution.data.Version
import com.auginte.distribution.data.{Description, Camera, Data}
import com.auginte.zooming.{AbsoluteDistance, Grid}

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

  override def parameters_=(values: List[Symbol]): Unit = _parameters = values

  override def load(): Unit = {
      println(s"Loading ${_parameters}")
  }

  val a = List[Int]()
  a.size

  override def description: Description = Description(softwareVersion, elements().size, cameras().size)
}
