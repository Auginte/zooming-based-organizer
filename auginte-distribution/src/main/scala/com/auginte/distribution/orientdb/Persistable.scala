package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}
import java.{lang => jl}

/**
 * Having link to OrientDb document, so setters and getters could be use to bind to database record.
 *
 * If not persisted, using object fields. If persisted, binds to OrientDb record:
 * {{{
 *   var _field = 123
 *   def field = get[Double]("field", _field)
 *   def filed_=(field: Double): Unit = set[Double]("field", field, _field = _)
 * }}}
 *
 * For data fields and connection separation and inheritance, record metadata is used:
 * {{{
 *   import PersistableImplicits._
 *   override protected[orientdb] def tableName: String = "ClassName"
 *   override protected[orientdb] def fields = Map[String, (this.type) => Object](
 *       "field" -> (_.field.boxed)
 *   )
 * }}}
 */
trait Persistable[T] {
  this: T =>
  private var _persisted: Option[ODocument] = None

  protected def get[A](parameter: String, default: A): A = persisted match {
    case Some(p) if default.isInstanceOf[Double] => // For: "java.lang.Float cannot be cast to java.lang.Double"
      p.field[Object](parameter) match {
        case f: jl.Float => f.toDouble.asInstanceOf[A]
        case value => value.asInstanceOf[A]
      }
    case Some(p) => try {
        p.field[A](parameter)
      } catch {
        case _: Throwable => default
      }
    case None => default
  }

  protected def set[A](parameter: String, value: A, default: A => Unit): Unit =
    if (persisted.isDefined) persisted.get.field(parameter, value) else default(value)


  def persisted: Option[ODocument] = _persisted

  protected[orientdb] def persisted_=(document: ODocument): Unit = _persisted = Some(document)


  protected[orientdb] def tableName: String

  protected[orientdb] def fields: Map[String, this.type => Object]

  def storeTo(storage: OrientBaseGraph): OrientVertex = {
    val values = fields.map(d => d._1 -> d._2(this)).flatten(r => List(r._1, r._2))
    val vertex = storage.addVertex(s"class:$tableName", values.toSeq: _*)
    persisted = vertex.getRecord
    vertex
  }
}