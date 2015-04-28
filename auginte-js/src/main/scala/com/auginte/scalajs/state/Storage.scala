package com.auginte.scalajs.state

/**
 * Meta data about state relation to persistent storage.
 *
 * @param id unique id in storage
 * @param hash security hash as simplest authentication implementation
 */
case class Storage(id: Int = -1, hash: String = "new")
