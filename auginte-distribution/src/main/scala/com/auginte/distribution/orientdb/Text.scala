package com.auginte.distribution.orientdb

/**
 * Storing text in infinity zooming grid.
 */
class Text(var _text: String = "") extends Representation {

    import PersistableImplicits._

    override protected[orientdb] def tableName: String = "Text"

    override protected[orientdb] def fields = super.fields ++ Map[String, (this.type) => Object](
        "text" -> (_.text.boxed)
    )

    def text = get[String]("text", _text)

    def text_=(text: String): Unit = set[String]("text", text, _text = _)

    override def toString = "{Text: text=" + text + " " + super.toString + "}"
}

object Text {
    def apply(text: String): Text = new Text(text)

    def apply(text: String, r: Representation): Text = {
        val element = apply(text)
        element.representation = r
        element
    }

    def unapply(data: Text) = Some(data.text, data.representation)
}
