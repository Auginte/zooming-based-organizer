package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.auginte.zooming.{GlobalCoordinates, Coordinates}

/**
 * Having Distance and Node parameters to work with coordinates in Infinity zooming
 */
trait GlobalCoordinatesWrapper extends CoordinatesWrapper with NodeWrapper {

  def globalCoordinates: GlobalCoordinates = (node, coordinates)

  def globalCoordinates_=(pair: GlobalCoordinates): Unit = {
    pair._1 match {
      case n: Node =>
        node = n
        coordinates = pair._2
      case _ => Unexpected.state(s"Using Grid Node in OrientDB context: $pair in $this")
    }
  }
}
