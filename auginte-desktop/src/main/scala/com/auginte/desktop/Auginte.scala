package com.auginte.desktop

import java.awt.SplashScreen
import javafx.scene.image.Image

import com.auginte.common.settings.GlobalSettings
import com.auginte.distribution.orientdb.{Position, Cache}
import com.auginte.distribution.{orientdb => o}
import com.auginte.{zooming => z}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.control.{Label, Button}
import scalafx.scene.layout.{HBox, BorderPane}
import scalafx.Includes._
import scalafx.application.Platform
import akka.actor.{Props, ActorSystem}
import scalafx.stage.WindowEvent
import com.auginte.desktop.{actors => act}


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

  stage.onCloseRequest = (e: WindowEvent) => quit()

  def quit(): Unit = {
    Platform.exit()
  }

  stage.scene = new Scene {
    root = view1
  }

  val settings = new GlobalSettings
  settings.graphRepository = settings.graphRepository.updatedWith(parameters.named)
  view1.load(settings)

  val splash = SplashScreen.getSplashScreen
  if (splash != null) {
    splash.close()
  }
}