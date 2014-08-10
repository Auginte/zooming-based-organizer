package com.auginte.distribution.repository

import com.auginte.Version
import com.auginte.distribution.data.{Camera, Data}
import com.auginte.zooming.{AbsoluteDistance, Grid}

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

  def getVersion = Version.toString
}
