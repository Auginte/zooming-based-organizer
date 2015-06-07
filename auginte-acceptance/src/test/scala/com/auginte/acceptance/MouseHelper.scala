package com.auginte.acceptance

import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.{JavascriptExecutor, WebElement, Point}

/**
 * Simulating mouse events.
 * If available with browser, otherwise with React Javascript
 */
trait MouseHelper extends BrowserHelper {

  private var cursor = new Point(0, 0)
  private var lastElement: Option[WebElement] = None

  def mouseDown(element: WebElement): Unit = browserJsTest {
    new Actions(_).clickAndHold(element).perform()
  } {
    val reactId = element.getAttribute("data-reactid")
    cursor = center(element)
    lastElement = Some(element)
    _.executeScript(
      s"""
         |var element = document.querySelector('[data-reactid="$reactId"]')
         |React.addons.TestUtils.Simulate.mouseDown(element, {screenX: ${cursor.x}, screenY: ${cursor.y}})
      """.stripMargin)
  }

  def mouseMove(offsetX: Int, offsetY: Int): Unit = browserJsTest {
    new Actions(_).moveByOffset(offsetX, offsetY).perform()
  } {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    cursor = new Point(cursor.x + offsetX, cursor.y + offsetY)
    _.executeScript(
      s"""
         |var element = document.querySelector('[data-reactid="$reactId"]')
         |React.addons.TestUtils.Simulate.mouseMove(element, {screenX: ${cursor.x}, screenY: ${cursor.y}})
      """.stripMargin)
  }


  def mouseUp(): Unit = browserJsTest {
    new Actions(_).release().perform()
  } {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    _.executeScript(
      s"""
         |var element = document.querySelector('[data-reactid="$reactId"]')
         |React.addons.TestUtils.Simulate.mouseUp(element, {screenX: ${cursor.x}, screenY: ${cursor.y}})
      """.stripMargin)
  }

  def wheel(amount: Int, elementSelector: String, cursorX: Int = 0, cursorY: Int = 0) = jsOnly {
    _.executeScript(
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


  private def browserJsTest(browserBased: Browser => Any)(jsBased: JavascriptExecutor => Any): Unit = debugException {
    driver match {
      case headlessDriver: PhantomJSDriver => jsBased(headlessDriver)
      case webDriver => browserBased(webDriver)
    }
  }

  private def jsOnly(jsBased: JavascriptExecutor => Any): Unit = debugException(jsBased(driver))

  private def getLastElement: WebElement = lastElement match {
    case Some(e) => e
    case None => driver.findElementByName("body")
  }
}
