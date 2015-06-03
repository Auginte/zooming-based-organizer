package com.auginte.acceptance

import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.{WebElement, Point}

trait MouseHelper extends BrowserHelper {

  private var cursor = new Point(0, 0)

  def mouseDown(element: WebElement): Unit = debugException {
    driver match {
      case d: PhantomJSDriver => phantomNotImplemented
      case d => new Actions(d).clickAndHold(element).perform()
    }
  }

  def mouseMove(offsetX: Int, offsetY: Int): Unit = debugException {
    driver match {
      case d: PhantomJSDriver => phantomNotImplemented
      case d => new Actions(d).moveByOffset(offsetX, offsetY).perform()
    }
  }


  def mouseUp() = debugException {
    driver match {
      case d: PhantomJSDriver => phantomNotImplemented
      case d => new Actions(driver).release().perform()
    }
  }

  def center(element: WebElement) =
    new Point(element.getLocation.x + element.getSize.width / 2, element.getLocation.y + element.getSize.height / 2)

  private def phantomNotImplemented = throw new NotImplementedError("PhantomJs does not fully support Mouse events")
}
