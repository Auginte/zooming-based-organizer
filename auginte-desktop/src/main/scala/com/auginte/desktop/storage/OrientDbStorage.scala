package com.auginte.desktop.storage

import com.auginte.distribution.orientdb.Structure

case class OrientDbStorage(name: String, connectionType: String = "plocal", user: String = "admin", password: String = "admin") {
  val db = Structure.createRepository(name, connectionType, user, password)
}
