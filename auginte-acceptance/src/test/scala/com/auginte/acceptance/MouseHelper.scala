package com.auginte.acceptance

import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.{JavascriptExecutor, WebElement, Point}

/**
 * Simulating mouse events.
 * If available with browser, otherwise with React Javascript
 */
trait MouseHelper extends BrowserHelper with PositionHelper {

  private var cursor = new Point(0, 0)
  private var lastElement: Option[WebElement] = None

  def mouseDown(element: WebElement): Unit = browserJsTest {
    new Actions(_).clickAndHold(element).perform()
  } {
    val reactId = element.getAttribute("data-reactid")
    cursor = center(element)
    lastElement = Some(element)
    _.executeScript(mosueEventScript(reactId, "mousedown", cursor))
  }

  def mouseMove(offsetX: Int, offsetY: Int): Unit = browserJsTest {
    new Actions(_).moveByOffset(offsetX, offsetY).perform()
  } {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    cursor = new Point(cursor.x + offsetX, cursor.y + offsetY)
    _.executeScript(mosueEventScript(reactId, "mousemove", cursor))
  }


  def mouseUp(): Unit = browserJsTest {
    new Actions(_).release().perform()
  } {
    val element = getLastElement
    val reactId = element.getAttribute("data-reactid")
    _.executeScript(mosueEventScript(reactId, "mouseup", cursor))
  }

  def wheel(amount: Int, elementSelector: String, cursorX: Int = 0, cursorY: Int = 0) = jsOnly {
    _.executeScript(wheelEventScript(elementSelector, amount, new Point(cursorX, cursorY)))
  }

  private def mosueEventScript(reactId: String, eventName: String, cursor: Point) =
    s"""
       |var element = document.querySelector('[data-reactid="$reactId"]');
       |var mouseEvent = document.createEvent("MouseEvent");
       |mouseEvent.initMouseEvent('$eventName', true, true, window, 1, ${cursor.x}, ${cursor.y}, ${cursor.x}, ${cursor.y}, false, false, false, false, 0, null);
       |element.dispatchEvent(mouseEvent);
      """.stripMargin

  private def wheelEventScript(selector: String, amount: Int, cursor: Point) =
    s"""
       |var element = document.querySelector('$selector');
       |var event;
       |try {
       |  event = new WheelEvent('wheel', {
       |    screenX: ${cursor.x}, screenY: ${cursor.y},
       |    clientX: ${cursor.x}, clientY: ${cursor.y},
       |    view: window, bubbles: true, cancellable: true,
       |    deltaY: $amount, wheelDelta: $amount, wheelDeltaY: $amount}
       |  );
       |} catch (e) {
       |  event = document.createEvent('WheelEvent');
       |  event.initWebKitWheelEvent(0, $amount, window,
       |    ${cursor.x}, ${cursor.y}, ${cursor.x}, ${cursor.y},
       |    false, false, false, false);
       |  event.deltaY = $amount;
       |}
       |element.dispatchEvent(event);
      """.stripMargin

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
