package com.auginte.distribution.data

/**
 * Container storing text.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Text extends Data {
  private var _text: String = ""

  def text = _text

  def text_=(value: String): Unit = _text = value

  override val storageFields: Map[String, () => String] = Map(
    "text" -> (() => text)
  )
}
