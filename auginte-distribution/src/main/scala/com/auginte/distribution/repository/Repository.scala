package com.auginte.distribution.repository

import java.io.{InputStream, IOException, OutputStream}

import com.auginte.distribution.data.{ImportedCamera, ImportedData, Description}
import com.auginte.zooming._

/**
 * Interface for modules with export/import/update capabilities
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Repository {
  @throws[IOException]
  def save(output: OutputStream, grid: Grid, elements: Elements, cameras: Cameras): Unit

  def load[A, B](input: InputStream,
                    dataFactory: (ImportedData, IdToRealNode) => A,
                    cameraFactory: (ImportedCamera, IdToRealNode) => B
                    ):
  (Grid, Seq[A], Seq[B])

}
