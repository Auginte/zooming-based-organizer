package com.auginte.scalajs.jshelpers

import scala.scalajs.js

/**
 * JavaScript ECMAScript 3rd
 */
@js.native
object URIHelper extends js.GlobalScope {
  def encodeURI(uri: String): String = js.native
}
