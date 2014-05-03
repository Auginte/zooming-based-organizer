package com.auginte.desktop

import scalafx.application.JFXApp
import scalafx.scene.shape.Rectangle
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

object HelloScalaFX extends JFXApp {
  val akka = ActorSystem("auginte")

  stage = new PrimaryStage {
    title = "Auginte"
    width = 600
    height = 450
  }

  val view1 = new View {
    prefWidth = 400.0
    prefHeight = 400.0
  }
  view1.stylesheets add "css/view.css"
  view1.stylesheets add "css/controls.css"
  view1.styleClass.add("view")

  val exitButton = new Button("Exit") {
    onAction = (e: ActionEvent) => quit()
  }

  val viewActor = akka.actorOf(Props[act.View], "view1")
  viewActor ! view1
  stage.onCloseRequest = ((e: WindowEvent) => quit())

  def quit(): Unit = {
    akka.shutdown()
    Platform.exit()
  }

  val infoLabel = new Label("Double-click to add new element/edit. Enter to finish.\n" +
    "Right click for context menu. Shift enter for new line.")

  stage.scene = new Scene {
    root = new BorderPane {
      center = view1
      bottom = new HBox {
        content = List(infoLabel, exitButton)
        spacing = 5
      }
    }
  }
}