package com.auginte.acceptance

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import org.openqa.selenium.{OutputType, TakesScreenshot, WebDriver}

/**
 * Helpers to debug intermediate results
 */
trait DebugHelper {
  def debug(driver: WebDriver): Unit = {
    val time = System.nanoTime()
    val tmpName = "/tmp/test-" + getClass.getName  +  time
    val targetHtml = new File(tmpName + ".html")
    saveFile(targetHtml.toPath, driver.getPageSource)
    driver match {
      case d: TakesScreenshot =>
        val screenshot = d.getScreenshotAs(OutputType.FILE)
        val targetImage = new File(tmpName + ".png")
        Files.copy(screenshot.toPath, targetImage.toPath)
        Runtime.getRuntime.exec("/usr/bin/chromium-browser " + targetHtml.getPath + " " + targetImage.getPath)
      case _ =>
    }
  }

  private def saveFile(path: Path, data: String): Path =
    Files.write(path, data.getBytes(StandardCharsets.UTF_8))
}
