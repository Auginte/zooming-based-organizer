package com.auginte.desktop

import scala.swing._
import scala.swing.event.ButtonClicked

/**
 * Main class for graphical user interface.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
object MainGui extends SimpleSwingApplication {
  val hello = new Label("Hello")

  def top: Frame = new MainFrame {
    title = "Auginte organiser"
    contents = new BorderPanel {
      layout(hello) = BorderPanel.Position.Center
      layout(exitButton) = BorderPanel.Position.South
    }
  }

  // Basic buttons
  val exitButton = Button("Exit") {
    reactions += {
      case ButtonClicked(source) => this.quit()
    }
  }
  listenTo(exitButton)
}
