package com.auginte.shared.state.persistable

/**
 * Meta data about state relation to persistent storage.
 *
 * @param id unique id in storage
 * @param hash security hash as simplest authentication implementation
 */
case class Storage(id: String = "", hash: String = "new")
