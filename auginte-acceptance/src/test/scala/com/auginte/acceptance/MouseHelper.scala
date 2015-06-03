package com.auginte.acceptance

import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.{WebElement, Point}

trait MouseHelper extends BrowserHelper {

  private var cursor = new Point(0, 0)

  def mouseDown(element: WebElement): Unit = debugException {
    new Actions(driver).clickAndHold(element).perform()
  }

  def mouseMove(offsetX: Int, offsetY: Int): Unit = debugException {
    new Actions(driver).moveByOffset(offsetX, offsetY).perform()
  }


  def mouseUp(): Unit = webDriverOnly {
    new Actions(driver).release().perform()
  }

  def wheel(amount: Int, elementSelector: String, cursorX: Int = 0, cursorY: Int = 0) = webDriverOnly {
    driver.executeScript(
      s"""
        |var area = document.querySelector('$elementSelector')
        |React.addons.TestUtils.Simulate.wheel(area, {nativeEvent: {deltaY: $amount}, clientX: $cursorX, clientY: $cursorY})
      """.stripMargin)
  }

  def center(element: WebElement) =
    new Point(element.getLocation.x + element.getSize.width / 2, element.getLocation.y + element.getSize.height / 2)

  def rightBottom(element: WebElement) =
    new Point(element.getLocation.x + element.getSize.getWidth, element.getLocation.y + element.getSize.height)

  private def phantomNotImplemented = throw new NotImplementedError("PhantomJs does not fully support Mouse events")

  private def webDriverOnly[A](f: => A) = debugException {
    driver match {
      case d: PhantomJSDriver => phantomNotImplemented
      case _ => f
    }
  }
}
