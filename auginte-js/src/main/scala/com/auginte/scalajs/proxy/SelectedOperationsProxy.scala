package com.auginte.scalajs.proxy

import com.auginte.shared.state.persistable.Persistable

abstract class SelectedOperationsProxy(val state: Persistable) extends EventProxy {
  def hasSelectedElements = state.container.elements.exists(_._2.selected == true)
}
