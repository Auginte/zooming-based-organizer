package com.auginte.distribution.orientdb


/**
 * Methods to force load or save throw database.
 *
 * Useful for slow database connections and other store optimisations.
 *
 * @see [[com.orientechnologies.orient.core.record.impl.ODocument#load]]
 * @see [[com.orientechnologies.orient.core.record.impl.ODocument#save]]
 */
trait DelayedPersistence {

  def updateDbToCached(): Unit = {}

  def updateCachedToDb(): Unit = {}
}
