package com.auginte.desktop

import com.auginte.distribution.repository.Repository

/**
 * Functionality for loading and saving data
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Storage {
  val repository: Repository
}
