package com.auginte.acceptance

import java.net.URL

import org.openqa.selenium.interactions.HasInputDevices
import org.openqa.selenium.internal._
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.{RemoteWebDriver, DesiredCapabilities}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium._

/**
 * Helpers for browser switch and common browser functions
 */
trait BrowserHelper extends DebugHelper {
  type Browser = WebDriver with JavascriptExecutor with FindsById with FindsByClassName with FindsByLinkText with FindsByName with FindsByCssSelector with FindsByTagName with FindsByXPath with HasInputDevices with HasCapabilities

  lazy val driver = sys.props.get("webDriver") match {
    case Some("chrome") | Some("chromium") =>
      val driverUrl = sys.props.getOrElse("driverUrl", default = "http://127.0.0.1:9515")
      val driverPath = sys.props.getOrElse("chromePath", default = "/usr/bin/chromium-browser")
      System.setProperty("webdriver.chrome.driver", driverPath)
      val capabilities = DesiredCapabilities.chrome()
      new RemoteWebDriver(new URL(driverUrl), capabilities)
    case _ => new PhantomJSDriver // Some("phantomJs")
  }

  lazy val baseUrl = sys.props.getOrElse("url", default = "http://127.0.0.1:9000")

  def element(cssSelector: String): WebElement = driver.findElementByCssSelector(cssSelector)

  def waitXpath(selector: String, timeout: Int = 5): WebElement = debugException {
    val waiting = new WebDriverWait(driver, timeout)
    waiting.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selector)))
  }

  def waitVisible(cssSelector: String, timeout: Int = 5): WebElement = debugException {
    val waiting = new WebDriverWait(driver, timeout)
    waiting.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)))
  }

  def waitState(cssSelector: String, timouet: Int = 5): Boolean = {
    val waitting = new WebDriverWait(driver, timouet)
    waitting until (ExpectedConditions stalenessOf element(cssSelector))
  }

  private def debugTimeOut(): Unit = {
//    debug(driver)
  }

  def xpath(selector: String): WebElement = driver.findElementByXPath(selector)

  def debugException[A](f: => A): A = try f catch {
    case e: Exception => debugTimeOut(); throw e
  }

  def mouse = driver.getMouse
}
