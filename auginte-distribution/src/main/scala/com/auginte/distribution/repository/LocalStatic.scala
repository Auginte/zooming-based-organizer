package com.auginte.distribution.repository

import com.auginte.distribution.data.Version
import com.auginte.distribution.data.{Description, Camera, Data}
import com.auginte.zooming.{AbsoluteDistance, Grid}
import com.{auginte => software}

/**
 * Simples repository saving everything to JSON files
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStatic(
                   grid: Grid,
                   elements: () => Seq[Data],
                   cameras: () => Seq[Camera],
                   converter: (Data) => Option[AbsoluteDistance])
  extends Repository {

  private var _parameters: List[Symbol] = List()

  override def save(): Unit = {
    println(s"Saving ${_parameters} ${elements()} ${cameras()}")
  }

  override def parameters_=(values: List[Symbol]): Unit = _parameters = values

  override def load(): Unit = {
    println(s"Loading ${_parameters}")
  }

  override def description: Description = Description(Version(software.SoftwareVersion.toString))
}
