package com.auginte.desktop

import java.awt.SplashScreen

import akka.actor.{ActorSystem, Props}
import com.auginte.desktop.{actors => act}
import com.auginte.zooming.Grid
import javafx.embed.swing.JFXPanel
import javafx.scene.image.Image
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.scene._
import scalafx.stage.WindowEvent

object Auginte extends JFXApp { app =>
  val akka = ActorSystem("auginte")

  // Shortcut to initialize JavaFX, force initialization by creating JFXPanel() object
  // (we will not use it for anything else)
  new JFXPanel()

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
    akka.terminate()
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