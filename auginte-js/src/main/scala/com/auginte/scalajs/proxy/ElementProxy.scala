package com.auginte.scalajs.proxy

import com.auginte.shared.state.persistable.{Camera, Element}

abstract class ElementProxy(val element: Element, val camera: Camera) extends EventProxy
