package com.auginte.desktop.storage

import scalafx.scene.Scene
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

/**
 * Using JavaFx file chooser for saving/loading data.
 * 
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait WithFileChooser extends UsingRepository {
  protected val extensionsFilter = new ExtensionFilter("Static Text repository (*.json)", "*.json")
  protected val nonBlockingWindow = new Scene().getWindow

  protected val fileChooser = new FileChooser {
    title = "Choose repository file"
    extensionFilters add extensionsFilter
  }

  protected def withExtension(path: String) = if (path.endsWith(".json")) path else path + ".json"
}
