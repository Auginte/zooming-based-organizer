package com.auginte.acceptance

trait ReactHelper extends BrowserHelper {
  def waitForReact() = {
    waitXpath("""//div[@data-reactid=".0"]""")
  } 
}
