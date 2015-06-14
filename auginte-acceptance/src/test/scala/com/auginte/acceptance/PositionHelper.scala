package com.auginte.acceptance

import org.openqa.selenium.{Point, WebElement}

/**
 * Common function to deal with positions
 */
trait PositionHelper {
  def center(element: WebElement) =
    new Point(element.getLocation.x + element.getSize.width / 2, element.getLocation.y + element.getSize.height / 2)

}
