package com.auginte.scalajs.events.logic

import com.auginte.shared.state.persistable.{Camera, Element}

case class ElementEvent(event: Event, element: Element, camera: Camera) extends Event
