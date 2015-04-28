package com.auginte.scalajs.jshelpers

import scala.scalajs.js

/**
 * JavaScript ECMAScript 3rd
 */
object URIHelper extends js.GlobalScope {
  def encodeURI(uri: String): String = js.native
}
