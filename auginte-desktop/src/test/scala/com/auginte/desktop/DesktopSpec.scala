package com.auginte.desktop

import com.auginte.test.UnitSpec

/**
 * Testing main functionality, available via graphical user interface
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class DesktopSpec extends UnitSpec {
  "Novice user" when {
    "need simplest task" should {
      "add new element via mouce click" in (pending)
      "edit multiline text" in (pending)
      "delete element" in (pending)
    }
    "need storage task" should {
      "save elements" in (pending)
      "load elements" in (pending)
    }
    "working with multiple elements" should {
      "select and deselect one element" in (pending)
      "select and deselect multiple elements" in (pending)
      "do bach operations selected elements" in (pending)
    }
  }
  "Advanced user" when {
    "need fast tasks" should {
      "get list of available commands" in (pending)
      "undo last command" in (pending)
      "redo last command" in (pending)
      "include third party plugins with new operations" in (pending) // Separating functionality to modules
    }
  }
  "Analytic" when {
    "projecting this software" should {
      "write ideas/requirements in list form" in (pending)
      "create implementation diagrams" in (pending)
      "connect requirements with implementation" in (pending)
    }
  }
}
