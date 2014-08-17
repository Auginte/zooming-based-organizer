package com.auginte.distribution

import com.auginte.distribution.data.{Camera, Data}
import com.auginte.zooming.AbsoluteDistance

/**
 * Controllers to store/load from static or dynamic data repositories
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
package object repository {
  type Elements = () => Seq[Data]
  type Cameras = () => Seq[Camera]
  type Converter = (Data) => Option[AbsoluteDistance]
}
