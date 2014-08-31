package com.auginte.desktop.storage

import com.auginte.distribution.repository.Repository

/**
 * Functionality for loading and saving data
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait UsingRepository {
  val repository: Repository
}
