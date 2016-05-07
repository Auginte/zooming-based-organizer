package com.auginte.distribution.orientdb

import com.auginte.distribution.orientdb.CommonSql._
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}
import scala.collection.JavaConversions._

/**
 * Camera with x,y,scale transformation of current view, attached to Node.
 *
 * If persisted, using default node.
 */
class Camera(var _x: Double = 0, var _y: Double = 0, var _scale: Double = 1)
  extends Persistable[Camera]
  with NodeLink[Camera]
  with CoordinatesWrapper with GlobalCoordinatesWrapper {

  import PersistableImplicits._

  override protected[orientdb] def tableName: String = "Camera"

  override protected[orientdb] def fields = Map[String, (this.type) => Object](
    "x" -> (_.x.boxed),
    "y" -> (_.y.boxed),
    "scale" -> (_.scale.boxed)
  )

  override protected def nodeEdge = (Direction.OUT, "View")


  def x = get[Double]("x", _x)

  def y = get[Double]("y", _y)

  def scale = get[Double]("scale", _scale)


  def x_=(x: Double): Unit = set[Double]("x", x, _x = _)

  def y_=(y: Double): Unit = set[Double]("y", y, _y = _)

  def scale_=(scale: Double): Unit = set[Double]("scale", scale, _scale = _)

  override def toString = "{Camera: x=" + x + ", y=" + y + ", scale=" + scale + " persisted=" + persisted + "}"
}

object Camera extends DefaultCache[Camera] {
  def apply(x: Double, y: Double, scale: Double): Camera = new Camera(x, y, scale)

  def apply(vertex: OrientVertex): Camera = new Camera() {
    persisted = vertex
  }

  def unapply(data: Camera) = Some(data.x, data.y, data.scale)

  def mainCamera(db: OrientBaseGraph, rootNode: Option[Node] = None)(implicit cache: Cached = defaultCache): Camera = {
    def withPersistable(persistable: OrientVertex) = cache(persistable.getIdentity) match {
      case Some(node) => node
      case None =>
        val camera = new Camera()
        camera.persisted = persistable
        cache += persistable.getIdentity -> camera
        camera
    }
    def withView(camera: Camera): Camera = {
      def addView(camera: OrientVertex, node: OrientVertex): Unit =
        reloadAnd(camera, node)(camera.addEdge("View", node))

      rootNode match {
        case Some(node) if camera.persisted.isDefined && node.persisted.isDefined =>
          addView(camera.persisted.get, node.persisted.get)
        case None =>
          addView(camera.persisted.get, Position.rootNode(db).persisted.get)
      }
      camera
    }
    val cameras = selectVertex(db)("SELECT FROM Camera LIMIT 1")
    val defaults = Seq("x", Double.box(0), "y", Double.box(0), "scale", Double.box(1))
    if (cameras.nonEmpty) withPersistable(cameras.head)
    else withView(withPersistable(db.addVertex("class:Camera", defaults: _*)))
  }
}
