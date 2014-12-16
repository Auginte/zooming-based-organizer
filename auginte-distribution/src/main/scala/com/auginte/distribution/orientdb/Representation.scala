package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}

class Representation(var _x: Double = 0, var _y: Double = 0, var _scale: Double = 1) {

  private def this(data: OrientVertex) {
    this()
    _persisted = Some(data.getRecord)
  }

  private var _persisted: Option[ODocument] = None


  def x = get[Double]("x", _x)

  def y = get[Double]("y", _y)

  def scale = get[Double]("scale", _scale)


  def x_=(x: Double): Unit = set[Double]("x", x, _x = _)

  def y_=(y: Double): Unit = set[Double]("y", y, _y = _)

  def scale_=(scale: Double): Unit = set[Double]("scale", scale, _scale = _)


  private[orientdb] def boxedX = Double.box(x)

  private[orientdb] def boxedY = Double.box(y)

  private[orientdb] def boxedScale = Double.box(scale)


  private def get[A <: Double](parameter: String, default: A): A = persisted match {
    case Some(p) =>
      try {
        p.field[A](parameter)
      } catch {
        case e: ClassCastException => e.getMessage match {
          case "java.lang.Float cannot be cast to java.lang.Double" =>
            p.field[Float](parameter).toDouble.asInstanceOf[A]
          case _ => default
        }
      }
    case None => default
  }

  private def set[A](parameter: String, value: A, default: A => Unit): Unit =
    if (persisted.isDefined) persisted.get.field(parameter, value) else default(value)

  def persisted: Option[ODocument] = _persisted

  override def toString = s"{Representation: x=$x, y=$y, scale=$scale}"
}

object Representation {
  def store(representation: Representation, storage: OrientBaseGraph): OrientVertex = {
    val r = representation
    val vertex = storage.addVertex("class:Representation", "x", r.boxedX, "y", r.boxedY, "scale", r.boxedScale)
    r._persisted = Some(vertex.getRecord)
    vertex
  }

  def load(elements: Iterable[OrientVertex]): Iterable[Representation] = elements.map(d => new Representation(d))
}