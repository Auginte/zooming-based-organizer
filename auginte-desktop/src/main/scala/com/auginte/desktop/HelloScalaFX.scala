package com.auginte.desktop

import scalafx.application.JFXApp
import scalafx.scene.shape.Rectangle
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, BorderPane}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.application.Platform


object HelloScalaFX extends JFXApp {
  stage = new PrimaryStage {
    title = "Auginte"
    width = 600
    height = 450
  }

  val view1 = new View {
    prefWidth = 400.0
    prefHeight = 400.0

    content = List(
      Label("Hello"),
      Rectangle(50, 50, 50, 50)
    )
  }
  view1.stylesheets add "css/view.css"
  view1.styleClass.add("view")

  val exitButton = new Button("Exit") {
    onAction = (e: ActionEvent) => Platform.exit()
  }
  val infoLabel = new Label("Double-click to add new elements")

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