package example.state

import example.state.selected.Selectable

/**
 * High level state data structure aggregating decoupled parts
 */
case class State(camera: Camera, container: Container, selected: Selectable, creation: Creation) {
  def inCamera(converter: Camera => Camera) = copy(camera = converter(camera))

  def inContainer(converter: Container => Container) = copy(container = converter(container))

  def inSelected(converter: Selectable => Selectable) = copy(selected = converter(selected))

  def inCreation(converter: Creation => Creation) = copy(creation = converter(creation))

  def and(convert: State => State) = convert(this)
}