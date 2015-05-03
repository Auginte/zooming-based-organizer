package com.auginte.scalajs

import com.auginte.scalajs.proxy.EventProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Helper for stateless, backend delegating React component
 */
abstract class SimpleComponent[A <: EventProxy](val componentName: String) {

  def build = ReactComponentB[A](componentName)
    .stateless
    .noBackend
    .render ((P, _, _) => generate(P))
    .build

  def r = build

  def generate(P: A): ReactTag

}
