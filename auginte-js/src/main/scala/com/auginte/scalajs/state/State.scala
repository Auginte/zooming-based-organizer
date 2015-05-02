package com.auginte.scalajs.state

import com.auginte.scalajs.state.persistable.{Persistable, Storage, Container, Camera}
import com.auginte.scalajs.state.selected.Selectable

/**
 * High level state data structure aggregating decoupled parts
 */
case class State(persistable: Persistable, creation: Creation, menu: Menu) {

  def inPersistable(converter: Persistable => Persistable) = copy(persistable = converter(persistable))

  def inCamera(converter: Camera => Camera) = inPersistable(_ copy(camera = converter(camera)))

  def inContainer(converter: Container => Container) = inPersistable(_ copy(container = converter(container)))

  def inSelected(converter: Selectable => Selectable) = inPersistable(_ copy(selected = converter(selected)))

  def inCreation(converter: Creation => Creation) = copy(creation = converter(creation))

  def inMenu(converter: Menu => Menu) = copy(menu = converter(menu))

  def inStorage(converter: Storage => Storage) = inPersistable(_ copy(storage = converter(storage)))

  def withStorage(newStorage: Storage): State = inPersistable(_ copy(storage = newStorage))

  def camera = persistable.camera

  def container = persistable.container

  def selected = persistable.selected

  def storage = persistable.storage

  def and(convert: State => State) = convert(this)
}