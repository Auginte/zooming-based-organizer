package com.auginte.desktop

import java.awt.SplashScreen
import javafx.scene.image.Image

import com.auginte.zooming.Grid

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

object Auginte extends JFXApp {
  val akka = ActorSystem("auginte")

  stage = new PrimaryStage {
    title = "Auginte"
    width = 600
    height = 450
    icons add new Image(getClass.getResourceAsStream("/com/auginte/common/splash.gif"))
  }

  val view1 = new View {
    prefWidth = 400.0
    prefHeight = 400.0
  }
  view1.stylesheets add "css/view.css"
  view1.stylesheets add "css/controls.css"
  view1.getStyleClass.add("view")

  val grid = new Grid
  val viewsSupervisor = akka.actorOf(Props[act.Views], "views")
  viewsSupervisor ! grid
  viewsSupervisor ! view1
  stage.onCloseRequest = (e: WindowEvent) => quit()

  def quit(): Unit = {
    akka.shutdown()
    Platform.exit()
  }

  val welcomeFile = "/examples/welcome.json"
  view1.loadFromStream(getClass.getResourceAsStream(welcomeFile))

  stage.scene = new Scene {
    root = view1
  }

  val splash = SplashScreen.getSplashScreen
  if (splash != null) {
    splash.close()
  }
}