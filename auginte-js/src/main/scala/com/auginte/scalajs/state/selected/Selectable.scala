package com.auginte.scalajs.state.selected

case class Selectable(elements: SelectedElements = SelectedElements(), camera: SelectedCamera = SelectedCamera()) {
  def inElements(converter: SelectedElements => SelectedElements) = copy(elements = converter(elements))

  def inCamera(converter: SelectedCamera => SelectedCamera) = copy(camera = converter(camera))
}