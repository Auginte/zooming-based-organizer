package com.auginte

/**
 * Package is responsible for infinity zooming,
 * by preserving hierarchy of nodes and
 * proving zooming/translation related helpers.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
package object zooming {
  /**
   * (X, Y, Scale) in textual representation.
   * E.g. ("1234567890123", "12345678", "124567891023456789")
   */
  type TextualCoordinates = (String, String, String)
}
