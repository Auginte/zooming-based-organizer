package com.auginte.distribution.orientdb

/**
 * Storing image in infinity zooming grid.
 */
class Image(var _path: String = "") extends Representation {

    import PersistableImplicits._

    override protected[orientdb] def tableName: String = "Image"

    override protected[orientdb] def fields = super.fields ++ Map[String, (this.type) => Object](
        "path" -> (_.path.boxed)
    )

    def path = get[String]("path", _path)

    def path_=(path: String): Unit = set[String]("path", path, _path = _)

    override def toString = "{Image: path=" + path + " {" + super.toString + "}}"
}

object Image {
    def apply(path: String): Image = new Image(path)

    def apply(path: String, r: Representation): Image = {
        val element = apply(path)
        element.representation = r
        element
    }

    def unapply(data: Image) = Some(data.path, data.representation)
}