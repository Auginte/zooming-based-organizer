package com.auginte.desktop.nodes

import javafx.scene.{layout => jfxl}

import com.auginte.common.SoftwareVersion
import com.auginte.desktop.rich.RichNode

import scala.io.Source
import scalafx.event.ActionEvent
import scalafx.scene.web.WebView
import scalafx.scene.control.Button
import scalafx.scene.{control => sfxc, layout => sfxl}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.Includes._

trait HelpWrapper extends RichNode[jfxl.Pane] {
  self: sfxl.Pane =>
  private val webView = new WebView {
    styleClass.add("webView")
  }
  val helpText = Source.fromInputStream(getClass.getResourceAsStream("/help/QuickIntro.html"))
  webView.getEngine.loadContent(helpText.mkString.replaceAll("VERSION", SoftwareVersion.toString))

  private val title = new sfxc.Label("Quick intro into Auginte")

  stylesheets add "css/contextMenu.css"
  styleClass.add("contextMenu")

  private val hideHelpButton = new Button("X") {
    styleClass.add("contextMenuButton")
  }

  hideHelpButton.prefWidth = 30
  title.prefWidth <== width - hideHelpButton.prefWidth

  private val helpPanel = new VBox {
    children = List(
      new HBox {
        children = List(
          title,
          hideHelpButton
        )
      },
      webView
    )
  }
  helpPanel.prefHeight <== height / 2

  hideHelpButton.onAction = (e: ActionEvent) => helpPanel.visible = false


  def showHelpWindow(): Unit = {
    if (!children.contains(helpPanel)) {
      children.add(helpPanel)
    }
    helpPanel.visible = true
  }

  def hideHelpWindow(): Unit = {
    helpPanel.visible = false
  }
}
