package com.auginte.desktop

import java.awt.Desktop
import java.net.URI
import javafx.animation.KeyFrame
import javafx.scene.layout.{Pane => jp}

import com.auginte.common.SoftwareVersion
import com.auginte.desktop.nodes.MouseFocusable
import com.auginte.desktop.operations.{ContextMenu, ContextMenuWrapper}
import com.auginte.desktop.persistable._
import com.auginte.desktop.rich.RichSPane
import com.auginte.distribution.orientdb.Representation
import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.distribution.orientdb._
import com.auginte.zooming.Grid
import com.auginte.distribution.{orientdb => o}
import com.auginte.desktop.{nodes => n}
import com.auginte.{zooming => z}
import javafx.{scene => jfxs}
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.event.ActionEvent
import javafx.scene.DepthTest
import scalafx.scene.control.Label
import scalafx.scene.input.MouseEvent
import scalafx.util.Duration
import scala.collection.JavaConversions._
import scalafx.application.Platform

/**
 * Viewing data in repository
 */
class RepositoryView extends RichSPane
with DragableView[jp] with MouseZoom[jp]
with MouseFocusable[jp]
with persistable.View
with persistable.Container
with CameraWrapper
with GridWrapper
with DatabaseWrapper
with ContextMenuWrapper
{

  val creator: Creator = {
    case "Text" => new n.Text()
    case _ => RepresentationWrapper(new Representation())
  }

  //
  // Absolute coordinates
  //

  private val grid2absoluteCron = new Timeline {
    cycleCount = Timeline.INDEFINITE
    keyFrames = Seq(
      jfxKeyFrame2sfx(new KeyFrame(
        Duration(10),
        (e: ActionEvent) =>
          if (grid.isDefined && db.isDefined && camera.isDefined) absoluteToCachedCoordinates() else renderLoading()
      ))
    )
  }
  grid2absoluteCron.play()

  def absoluteToCachedCoordinates(): Unit = {
    loading.visible = false
    val zoomableElements = d.getChildren.iterator().flatMap({
      case r: GuiRepresentation => Some(r)
      case _ => None
    })
    ODatabaseRecordThreadLocal.INSTANCE.set(db.get.getRawGraph)
    absoluteToCachedCoordinates(zoomableElements)
  }

  private def absoluteToCachedCoordinates(node: GuiRepresentation): Unit = {
    node.setLayoutX(node.storage.x + camera.get.x)
    node.setLayoutY(node.storage.y + camera.get.y)
  }


  //
  // Adding
  //

  mouseClicked += {
    (e: MouseEvent) => if (e.clickCount > 1 && camera.isDefined) createText(e)
  }

  def createText(e: MouseEvent): Unit = {
    val text = new n.Text
    text.storage.representation = Representation(e.x, e.y, 1)
    persist(text, camera.get.node)
    fromCameraView(text, e.x, e.y)
    text.view = this
    absoluteToCachedCoordinates(text)
    d.getChildren.add(text)
    text.editable = true
  }

  private def persist(data: RepresentationWrapper, cameraNode: o.Node): Unit = db match {
    case Some(db) =>
      ODatabaseRecordThreadLocal.INSTANCE.set(db.getRawGraph)
      val representation = data.storage.representation
      representation.storeTo(db)
      representation.node = cameraNode
    case None => throw new RuntimeException(s"No database to store: $data")
  }



  //
  // Loading elements
  //

  override def updateCachedToDb(): Unit = camera match {
    case Some(camera) => camera.save()
    case None => Unit
  }

  def loadElements(db: OrientBaseGraph): Unit = {
    def select(sql: String) = new OSQLSynchQuery[ODocument](sql)
    def addRepresentations(representations: Iterable[RepresentationWrapper]): Unit = {
      ODatabaseRecordThreadLocal.INSTANCE.set(db.getRawGraph)
      d.getChildren.addAll(representations.flatMap({
        case n: jfxs.Node => Some(n)
        case _ => None
      }))
      for(representation <- representations) {
        representation.updateDbToCached()
      }
    }
    val rows = select("SELECT @rid FROM Node")
    val nodes = o.Node.load(db, rows)
    val creator: Creator = {
      case "Text" => new n.Text() {
        view = RepositoryView.this
      }
      case r => throw new RuntimeException(s"Not valid representation name: $r")
    }
    for (node <- nodes) {
      val representations = node.representations(creator)
      Platform.runLater(addRepresentations(representations))
    }
  }

  //
  // Loading
  //

  val loading = new Label("Loading database...")
  d.getChildren.add(loading)

  private var angle = 0

  def renderLoading(): Unit = {
    loading.rotate <== angle
    angle = angle + 1
    if (angle > 360) {
      angle = angle - 360
    }
    loading.layoutX <== width / 2 - loading.width / 2
    loading.layoutY <== height / 2 - loading.height / 2
  }

  def renderError(error: String): Unit = loading.text = error


  //
  // Operations
  //

  override def operations: Operations = Map(
    "Exit" -> ((e: ActionEvent) => Auginte.quit()),
    "Feedback" -> ((e: ActionEvent) => feedback())
  )

  private def feedback(): Unit = {
    try {
      val version = SoftwareVersion.toString
      Desktop.getDesktop.mail(new URI(s"mailto:aurelijus@auginte.com?subject=Feedback%20$version"))
    } catch {
      case e: Exception => Desktop.getDesktop.browse(new URI(s"http://auginte.com/en/contact"))
    }
  }
}
