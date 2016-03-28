package com.auginte

import scala.scalajs.js

package object scalajs {
  @js.native
  trait ClientRect extends js.Object {
    def left: Int = js.native
    def top: Int = js.native
    def right: Int = js.native
    def bottom: Int = js.native
  }

  @js.native
  trait CSSOMView extends js.Object {
    def getBoundingClientRect(): ClientRect = js.native
  }
}
