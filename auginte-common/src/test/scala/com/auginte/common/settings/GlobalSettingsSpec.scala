package com.auginte.common.settings

import java.util.prefs.Preferences

import com.auginte.test.UnitSpec

/**
 * Unit tests for [[GlobalSettings]]
 *
 * On linux Preference API save files to ``~/.java/.userPrefs/com/auginte/common/settings``
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class GlobalSettingsSpec extends UnitSpec {
  "Global Settings storage" when {
    "executed first time" should {
      "save defaults with local OrientDB repository in home directory" in {
        val settings = new GlobalSettings
        val graphDir = GlobalSettings.localGraphDirectory
        settings.clear()
        val repository = settings.graphRepository
        assert("plocal" === repository.connection)
        assert(graphDir === repository.name)
        assert("admin" === repository.user)
        assert("admin" === repository.password)

        val preferences = Preferences.userNodeForPackage(settings.getClass)
        assert("plocal" === preferences.get("GraphRepository.connection", ""))
        assert(graphDir === preferences.get("GraphRepository.name", ""))
        assert("admin" === preferences.get("GraphRepository.user", ""))
        assert("admin" === preferences.get("GraphRepository.password", ""))
      }
    }
    "have settings already saved" should {
      "load graph repository settings" in {
        val settings = new GlobalSettings
        val preferences = Preferences.userNodeForPackage(settings.getClass)
        preferences.clear()
        preferences.put("GraphRepository.connection", "remote")
        preferences.put("GraphRepository.name", "localhost/augintetests")
        preferences.put("GraphRepository.user", "root")
        preferences.put("GraphRepository.password", "secret")
        val repository = settings.graphRepository
        assert("remote" === repository.connection)
        assert("localhost/augintetests" === repository.name)
        assert("root" === repository.user)
        assert("secret" === repository.password)
      }
      "save graph repository settings from object notation " in {
        val settings = new GlobalSettings
        val preferences = Preferences.userNodeForPackage(settings.getClass)
        preferences.clear()
        val updated = new GraphRepository("memory", "test", "some", "password")
        settings.graphRepository = updated
        assert(updated.connection === preferences.get("GraphRepository.connection", ""))
        assert(updated.name === preferences.get("GraphRepository.name", ""))
        assert(updated.user === preferences.get("GraphRepository.user", ""))
        assert(updated.password === preferences.get("GraphRepository.password", ""))
      }
      "output current settings as string" in {
        val settings = new GlobalSettings
        val preferences = Preferences.userNodeForPackage(settings.getClass)
        preferences.clear()
        settings.graphRepository = new GraphRepository("memory", "test", "foo", "bar")
        val expected = "{GraphRepository: connection=memory, name=test, user=foo, password=bar}"
        assert(expected === settings.toString)
      }
    }
  }
}