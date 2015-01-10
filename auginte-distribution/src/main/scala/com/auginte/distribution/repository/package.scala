package com.auginte.distribution

import com.auginte.distribution.data.{Camera, Data}
import com.auginte.zooming.{Grid, GlobalCoordinates}

import scala.collection.Traversable

/**
 * Controllers to store/load from static or dynamic data repositories
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
package object repository {
  type Elements = () => Traversable[Data]
  type Cameras = () => Traversable[Camera]
  type Converter = (Data) => Option[GlobalCoordinates]

  type SavedRepository = (Grid, Elements, Cameras)

  type CustomFields = Map[String, String]
}
