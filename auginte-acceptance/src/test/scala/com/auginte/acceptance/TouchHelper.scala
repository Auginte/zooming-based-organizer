package com.auginte.acceptance

import org.openqa.selenium.{JavascriptExecutor, Point, WebElement}

/**
 * Simulating touch events
 * using React test utils to simulate events
 */
trait TouchHelper extends BrowserHelper with ReactHelper with PositionHelper {

  private var cursor = new Point(0, 0)
  private var lastElement: Option[WebElement] = None

  def touchDown(element: WebElement): Unit = jsOnly {
    val reactId = element.getAttribute("data-reactid")
    cursor = center(element)
    lastElement = Some(element)
    _.executeScript(touchEventScript(reactId, "touchStart", cursor))
  }

  def touchMove(offsetX: Int, offsetY: Int): Unit = jsOnly {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    cursor = new Point(cursor.x + offsetX, cursor.y + offsetY)
    _.executeScript(touchEventScript(reactId, "touchMove", cursor))
  }

  def touchUp(): Unit = jsOnly {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    _.executeScript(touchEventScript(reactId, "touchEnd", cursor))
  }

  private def jsOnly(jsBased: JavascriptExecutor => Any): Unit = debugException(jsBased(driver))

  private def getLastElement: WebElement = lastElement match {
    case Some(e) => e
    case None => driver.findElementByName("body")
  }
}
