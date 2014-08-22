package com.auginte.distribution.repository

import com.auginte.distribution.data.Description

/**
 * Interface for modules with export/import/update capabilities
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Repository {
  def save(): Unit

  def load(): Unit

  def parameters_=(values: List[Symbol])

  def description: Description
}
