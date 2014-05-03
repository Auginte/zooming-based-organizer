package com.auginte.desktop.rich

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}

/**
 * Delegating functionality to JavaFx Node children
 *
 * @tparam D JavaFx class to use with `delegate` value
 */
trait RichNodeDelegating[D <: jfxs.Node] {
  /**
   * JavaFx element to delegate all rich functionality.
   * Define concrete implementation of d in trait, not in class for Linearization Algorithm to work as expected.
   *
   * When extending JavaFx:
   * {{{
   *   override val d = this
   * }}}
   *
   * When extending ScalaFx:
   * {{{
   *   override val d = delegate
   * }}}
   */
  protected val d: D
}