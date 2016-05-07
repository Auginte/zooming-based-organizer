package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.{Direction, Vertex}
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}
import scala.language.implicitConversions
import java.{lang => jl}
import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * Data representation in infinity zooming grid.
 *
 * Storing absolute coordinates (`x`, `y`, `scale`)
 *
 * Using without database (Scala object only)
 * {{{
 *   val r = Representation(1.1, 2.2, 3.3)
 *   println(s"${r.x} ${r.y} ${r.scale}")
 *   r.x = 3.3
 *   println(r)
 * }}}
 *
 * Inserting into database (binding to database object)
 * {{{
 *   val db = new OrientGraphNoTx(s"memory:databaseWithStructures")
 *   val r = Representation(1.1, 2.2, 3.3)
 *   r.store(db)
 *   r.x = 3.3
 * }}}
 *
 * Loading from database (binding to database object)
 * {{{
 *   val db = new OrientGraphNoTx(s"memory:databaseWithVertices")
 *   val creator: Creator = {
 *     case "Text" => new Text()
 *     case "Image" => new Image()
 *     case _ => new Representation()
 *   }
 *   val representations = Representation.load(db.getVertices, creator)
 *   representations.foreach {
 *     case Image(path, representation) => // Working with Image representation
 *     case Text(text, representation) => // Working with Text representation
 *     case representation: Representation => // Working with other types
 *   }
 * }}}
 */
class Representation(var _x: Double = 0, var _y: Double = 0, var _scale: Double = 1)
  extends Persistable[Representation]
  with NodeLink[Representation]
  with GlobalCoordinatesWrapper
  with Cloneable {

  import PersistableImplicits._
  import Representation._

  override protected[orientdb] def tableName: String = "Representation"

  override protected[orientdb] def fields: Map[String, (this.type) => Object] = Map(
    "x" -> (_.x.boxed),
    "y" -> (_.y.boxed),
    "scale" -> (_.scale.boxed)
  )

  override protected def nodeEdge: (Direction, String) = (Direction.OUT, "Inside")

  def x = get[Double]("x", _x)

  def y = get[Double]("y", _y)

  def scale = get[Double]("scale", _scale)


  def x_=(x: Double): Unit = set[Double]("x", x, _x = _)

  def y_=(y: Double): Unit = set[Double]("y", y, _y = _)

  def scale_=(scale: Double): Unit = set[Double]("scale", scale, _scale = _)


  def representation: Representation = this

  def representation_=(r: Representation): Unit = {
    x = r.x
    y = r.y
    scale = r.scale
  }

  def remove(): Unit = persisted match {
    case Some(persisted) => persisted.remove()
    case _ => Unit
  }


  def storeTo(storage: OrientBaseGraph, wrapper: RepresentationWrapper)(implicit cache: Cached = defaultCache): OrientVertex = {
    val vertex = super.storeTo(storage)
    cache += vertex.getIdentity -> wrapper
    vertex
  }

  override def toString = s"{Representation: x=$x, y=$y, scale=$scale}"

  /** Only shallow cloning */
  override def clone(): this.type = super.clone().asInstanceOf[this.type]

  def canEqual(other: Any): Boolean = other.isInstanceOf[Representation]

  override def equals(other: Any): Boolean = other match {
    case that: Representation =>
      (that canEqual this) &&
        x == that.x &&
        y == that.y &&
        scale == that.scale
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(_x, _y, _scale)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Representation extends DefaultCache[RepresentationWrapper] {
  type Creator = (String) => RepresentationWrapper

  def apply(data: OrientVertex): Representation = new Representation() {
    persisted = data
  }

  def apply(x: Double, y: Double, scale: Double) = new Representation(x, y, scale)

  def unapply(r: Representation): Option[(Double, Double, Double)] = Some((r.x, r.y, r.scale))

  def loadAll(rows: jl.Iterable[_ <: Vertex], creator: Creator)(implicit cache: Cached = defaultCache): Iterable[RepresentationWrapper] =
    toOrientVertex(rows) map (load(_, creator))

  private def toOrientVertex(rows: jl.Iterable[_ <: Vertex]) = iterableAsScalaIterable(rows) flatMap {
    case v: OrientVertex => Some(v)
    case _ => None
  }

  def load(vertex: OrientVertex, creator: Creator)(implicit cache: Cached = defaultCache): RepresentationWrapper = {
    val wrapperByClass = creator(vertex.getRecord.getClassName)
    wrapperByClass.storage.persisted = vertex
    cache += vertex.getIdentity -> wrapperByClass
    wrapperByClass
  }
}
