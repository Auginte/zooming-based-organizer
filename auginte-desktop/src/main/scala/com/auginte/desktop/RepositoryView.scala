package com.auginte.desktop

import java.awt.Desktop
import java.net.URI
import javafx.animation.KeyFrame
import javafx.scene.layout.{Pane => jp}

import com.auginte.common.SoftwareVersion
import com.auginte.common.settings.GlobalSettings
import com.auginte.desktop.nodes.{HelpWrapper, MouseFocusable}
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
import com.orientechnologies.common.exception.OException
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.event.ActionEvent
import javafx.scene.DepthTest
import scalafx.scene.control.Label
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
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
with RenderingConnections
with HelpWrapper
{
  private var loadingElements: Boolean = true

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
          if (!loading) absoluteToCachedCoordinates() else renderLoading()
      ))
    )
  }
  grid2absoluteCron.play()

  def absoluteToCachedCoordinates(): Unit = {
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
      representation.storeTo(db, data)
    case None => throw new RuntimeException(s"No database to store: $data")
  }



  //
  // Loading elements
  //

  override def updateCachedToDb(): Unit = camera match {
    case Some(camera) => camera.save()
    case None => Unit
  }

  private def loadElements(db: OrientBaseGraph): Unit = {
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

  private def loading = loadingElements || grid.isEmpty || db.isEmpty || camera.isEmpty

  def load(settings: GlobalSettings): Unit = {
    val loadingFromDatabase = new Thread {
      override def run(): Unit = try {
        val gs = settings.graphRepository
        val graphDatabase = Structure.createRepository(gs.name, gs.connection, gs.user, gs.password)
        ODatabaseRecordThreadLocal.INSTANCE.set(graphDatabase.getRawGraph)
        db = graphDatabase
        camera = Camera.mainCamera(graphDatabase)
        val newGrid = new Grid(Position.rootNode(graphDatabase))
        grid = newGrid
        loadElements(graphDatabase)
        Platform.runLater(absoluteToCachedCoordinates())
        loadingElements = false
        Platform.runLater(loadingLabel.visible = false)
      } catch {
        case e: OException =>
          System.err.println(e)
          renderError("Cannot open database. Others are using it?")
      }
    }
    loadingFromDatabase.start()
    showHelpWindow()
  }

  val loadingLabel = new Label("Loading database...")
  d.getChildren.add(loadingLabel)

  private var angle = 0

  def renderLoading(): Unit = if (loadingElements) {
    loadingLabel.rotate <== angle
    angle = angle + 1
    if (angle > 360) {
      angle = angle - 360
    }
    loadingLabel.layoutX <== width / 2 - loadingLabel.width / 2
    loadingLabel.layoutY <== height / 4 * 3 - loadingLabel.height / 2
  } else {
    loadingLabel.rotate <== 0
    loadingLabel.layoutY <== height / 4 * 3 - loadingLabel.height / 2
  }

  def renderError(error: String): Unit = Platform.runLater{
    loadingLabel.styleClass.add("error")
    loadingLabel.text = error
    loadingElements = false
  }


  //
  // Operations
  //

  override def operations: Operations = Map(
    "Exit" -> ((e: ActionEvent) => Auginte.quit()),
    "Feedback" -> ((e: ActionEvent) => feedback()),
    "Help" -> ((e: ActionEvent) => showHelpWindow())
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
