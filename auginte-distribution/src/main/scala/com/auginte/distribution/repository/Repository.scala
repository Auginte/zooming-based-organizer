package com.auginte.distribution.repository

/**
 * Interface for modules with export/import/update capabilities
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Repository {
  def save()
  def load()
  def parameters_=(values: List[Symbol])
}
