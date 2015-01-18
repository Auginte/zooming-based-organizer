package com.auginte.common.settings

import java.util.prefs.Preferences

/**
 * Settings for default data repository.
 *
 * This storage does not inlude images or other large raw data.
 *
 * @param connection E.g. plocal, remote
 * @param name E.g. auginte
 * @param user E.g. admin
 * @param password E.g. admin
 */
class GraphRepository(val connection: String, val name: String, val user: String, val password: String) {

  import GraphRepository.set

  def storeTo(preferences: Preferences): Unit = {
    set(preferences, "connection", connection)
    set(preferences, "name", name)
    set(preferences, "user", user)
    set(preferences, "password", password)
  }

  override def toString: String =
    s"{GraphRepository: connection=$connection, name=$name, user=$user, password=$password}"
}

object GraphRepository {
  private val preferencesPrefix = "GraphRepository"

  def apply(preferences: Preferences): GraphRepository = new GraphRepository(
    get(preferences, "connection", default = "plocal"),
    get(preferences, "name", default = GlobalSettings.localGraphDirectory),
    get(preferences, "user", default = "admin"),
    get(preferences, "password", default = "admin")
  )

  def unapply(r: GraphRepository) = Some((r.connection, r.name, r.user, r.password))

  private def canonical(field: String) = s"$preferencesPrefix.$field"

  private def get(preferences: Preferences, field: String, default: String = "") =
    preferences.get(canonical(field), default)

  private def set(preferences: Preferences, field: String, value: String): Unit =
    preferences.put(canonical(field), value)
}