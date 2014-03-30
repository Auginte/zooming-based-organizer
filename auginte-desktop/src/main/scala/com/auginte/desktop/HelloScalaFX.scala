package com.auginte.desktop

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.application.JFXApp.PrimaryStage

object HelloScalaFX extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaFX Hello World"
    width = 600
    height = 450
    scene = new Scene {
      fill = Color.LIGHTGREEN
      content = Set(new Rectangle {
        x = 25
        y = 40
        width = 100
        height = 100
        fill <== when(hover) choose Color.GREEN otherwise Color.RED
      })
    }

  }
}