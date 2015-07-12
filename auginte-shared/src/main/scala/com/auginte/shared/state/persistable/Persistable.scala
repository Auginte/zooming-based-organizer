package com.auginte.shared.state.persistable

import com.auginte.shared.Version
import com.auginte.shared.state.selected.Selectable

case class Persistable(camera: Camera, container: Container, selected: Selectable, storage: Storage, version: String = Version.textual) {
  def inCamera(converter: Camera => Camera) = copy(camera = converter(camera))

  def inContainer(converter: Container => Container) = copy(container = converter(container))

  def inSelected(converter: Selectable => Selectable) = copy(selected = converter(selected))

  def inStorage(converter: Storage => Storage) = copy(storage = converter(storage))

  def withStorage(newStorage: Storage): Persistable = copy(storage = newStorage)

  def and(convert: Persistable => Persistable) = convert(this)
}