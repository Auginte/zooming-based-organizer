package com.auginte.server.migration

import com.auginte.shared.Version
import org.scalatest.WordSpec

/**
 * Test for [[com.auginte.server.migration.PersistableMigration]]
 */
class PersistableMigrationSpec extends WordSpec {
  "Migrating from v0.8.3 to v0.8.4" should {
    "fill default scale" in {
      val latestVersion = Version.textual
      val input =
        """
          |{"#id": "17", "camera": {"x": -0.8892000000000024, "#id": "1", "y": -3.9129999999999967, "scale": 0.22089999999999999, "selected": false}, "container": {"#id": "6", "elements": {"#id": "5", "#elems": [["0", {"x": 33.57679999999999, "#id": "2", "y": 12.59129999999999, "height": 20, "selected": true, "text": "Testas", "id": "0", "width": 50}], ["1", {"x": 162, "#id": "3", "y": 29, "height": 20, "selected": false, "text": "Vienas", "id": "1", "width": 50}], ["2", {"x": 0, "#id": "4", "y": 40, "height": 20, "selected": false, "text": "Du", "id": "2", "width": 50}]]}, "nextId": 3}, "selected": {"#id": "15", "elements": {"#id": "9", "id": {"#id": "7", "#elems": []}, "last": {"#id": "8", "x": 22.531799999999997, "y": 82.17479999999999}}, "camera": {"#id": "14", "cameraId": {"#ref": "7"}, "movable": {"#id": "11", "last": {"#id": "10", "x": -134.749, "y": -135.6326}}, "zoomable": {"#id": "13", "last": {"#id": "12", "x": 0, "y": 0}, "lastDistance": -1, "currentDistance": -1}}}, "storage": {"#id": "16", "id": "", "hash": "r0EGy4n7d7YcFqrwkhGMkJtApL53FcBjyGvAqi2eWzm1DqgBRLFuVLG8Ct1WS9Ml"}}
        """.stripMargin.trim

      val migrated =
        s"""
          |{"#id": "17", "camera": {"x": -0.8892000000000024, "#id": "1", "y": -3.9129999999999967, "scale": 0.22089999999999999, "selected": false}, "container": {"#id": "6", "elements": {"#id": "5", "#elems": [["0", {"x": 33.57679999999999, "#id": "2", "y": 12.59129999999999, "scale": 1.0, "height": 20, "selected": true, "text": "Testas", "id": "0", "width": 50}], ["1", {"x": 162, "#id": "3", "y": 29, "scale": 1.0, "height": 20, "selected": false, "text": "Vienas", "id": "1", "width": 50}], ["2", {"x": 0, "#id": "4", "y": 40, "scale": 1.0, "height": 20, "selected": false, "text": "Du", "id": "2", "width": 50}]]}, "nextId": 3}, "selected": {"#id": "15", "elements": {"#id": "9", "id": {"#id": "7", "#elems": []}, "last": {"#id": "8", "x": 22.531799999999997, "y": 82.17479999999999}}, "camera": {"#id": "14", "cameraId": {"#ref": "7"}, "movable": {"#id": "11", "last": {"#id": "10", "x": -134.749, "y": -135.6326}}, "zoomable": {"#id": "13", "last": {"#id": "12", "x": 0, "y": 0}, "lastDistance": -1, "currentDistance": -1}}}, "version": "$latestVersion", "storage": {"#id": "16", "id": "", "hash": "r0EGy4n7d7YcFqrwkhGMkJtApL53FcBjyGvAqi2eWzm1DqgBRLFuVLG8Ct1WS9Ml"}}
        """.stripMargin.trim

      assert(PersistableMigration.migrate(input) === migrated)
    }
  }
}
