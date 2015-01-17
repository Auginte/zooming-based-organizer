package com.auginte.desktop

import java.awt.SplashScreen
import javafx.scene.image.Image

import com.auginte.desktop.storage.OrientDbStorage
import com.auginte.distribution.orientdb.{Position, Cache}
import com.auginte.zooming.Grid
import com.auginte.distribution.{orientdb => o}
import com.auginte.{zooming => z}
import com.orientechnologies.common.io.OIOException
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.auginte.distribution.orientdb.Camera
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.control.{Label, Button}
import scalafx.scene.layout.{HBox, BorderPane}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.application.Platform
import akka.actor.{Props, ActorSystem}
import scalafx.stage.WindowEvent
import com.auginte.desktop.{actors => act}
import scala.collection.mutable


object Auginte extends JFXApp {
  stage = new PrimaryStage {
    title = "Auginte"
    width = 600
    height = 450
    icons add new Image(getClass.getResourceAsStream("/com/auginte/common/splash.gif"))
  }


  val view1 = new RepositoryView() {
    prefWidth = 400.0
    prefHeight = 400.0
  }
  view1.stylesheets add "css/view.css"
  view1.stylesheets add "css/controls.css"
  view1.getStyleClass.add("view")

  val loadingFromDatabase = new Thread {
    override def run(): Unit = try {
      println("Connecting...")
      val storage = new OrientDbStorage("localhost/augintetests", "remote", "root", "kramtauauginte")
      val db = storage.db
      ODatabaseRecordThreadLocal.INSTANCE.set(db.getRawGraph)

      println("Connected.")

      val grid = new Grid(Position.rootNode(db))
      view1.grid = grid
      view1.db = db
      view1.camera = Camera.mainCamera(db)
      println("Grid replaced. Loading elements...")
      view1.loadElements(db)
      println("Elements loaded")
    } catch {
      case e: OIOException => view1.renderError("Cannot connect to database")
    }
  }

//  val grid = new Grid
//  val viewsSupervisor = akka.actorOf(Props[act.Views], "views")
//  viewsSupervisor ! grid
//  viewsSupervisor ! view1
  stage.onCloseRequest = (e: WindowEvent) => quit()

  def quit(): Unit = {
    Platform.exit()
  }

//  val welcomeFile = "/examples/welcome.json"
//  view1.loadFromStream(getClass.getResourceAsStream(welcomeFile))
//  view1.clearElements()

  stage.scene = new Scene {
    root = view1
  }
  loadingFromDatabase.start()

  val splash = SplashScreen.getSplashScreen
  if (splash != null) {
    splash.close()
  }
}