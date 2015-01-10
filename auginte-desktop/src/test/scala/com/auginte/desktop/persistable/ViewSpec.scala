package com.auginte.desktop.persistable

import com.auginte.test.UnitSpec
import com.auginte.distribution.orientdb.TestDbHelpers._
import com.auginte.desktop.persistable.TestGuiHelpers._
import com.auginte.zooming.Debug
import scala.collection.JavaConversions._

/**
 * Unit tests for [[com.auginte.desktop.persistable.View]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class ViewSpec extends UnitSpec {
  "Persistable View" when {
    "creating new elements" should {
      "update element's initial position by GUI" in {
        val db = newDb
        val view = newView(db)
        val text = newText(view)
        val cameraNode = view.camera.get.node
        val representation = text.storage
        representation.storeTo(db)
        representation.node = cameraNode
        val sql =
          """
            |SELECT x, $node.x AS nx, $representation.x AS rx, $representation.y AS ry
            |FROM Camera
            |LET $node = first(out_View), $representation = first($node.in_Inside)
          """.stripMargin
        val initialValues = select(sql).iterator().next()
        assert(0 === initialValues.field[Double]("x"))
        assert(0 === initialValues.field[Byte]("nx"))
        assert(0 === initialValues.field[Double]("rx"))
        assert(0 === initialValues.field[Double]("ry"))
        view.fromCameraView(text, 12, 34)
        val movedRepresentation = select(sql).iterator().next()
        assert(0 === movedRepresentation.field[Double]("x"))
        assert(0 === movedRepresentation.field[Byte]("nx"))
        assert(12 === movedRepresentation.field[Double]("rx"))
        assert(34 === movedRepresentation.field[Double]("ry"))
      }
      "translate element between nodes" in {
        //                  ____(0,0)_____
        //                /      t3       \
        //            (1,4)  t2      t4   (0,0)_ _ _ _ _ _ to old position (123. 456)
        //              /                   \
        // rep(23, 56, 1)  t1           t5   camera(0, 0, 1)
        val db = newDb
        val view = newView(db)
        val text = newText(view, "test2")
        val cameraNode = view.camera.get.node
        val representation = text.storage
        representation.storeTo(db)
        representation.node = cameraNode
        view.translate(text, 123, 456)
        val sql =
        """
          |SELECT
          |  x, y, text,
          |  $h2.x AS t2x, $h2.y AS t2y,
          |  $h3.x AS t3x, $h3.y AS t3y,
          |  $h4.x AS t4x, $h4.y AS t4y,
          |  $h5.x AS t5x, $h5.y AS t5y
          |FROM (
          |	 TRAVERSE * FROM Camera WHILE $depth <= 4
          |)
          |LET $h1 = traversedVertex(-1),
          |    $h2 = traversedVertex(-2),
          |    $h3 = traversedVertex(-3),
          |    $h4 = traversedVertex(-4),
          |    $h5 = traversedVertex(-5)
          |WHERE $depth = 4
          |  AND $h1.@class = "Text" AND $h1.out_Inside.@rid = $h2.@rid
          |  AND $h2.@class = "Node" AND $h2.out_Parent.@rid = $h3.@rid
          |  AND $h3.@class = "Node"
          |  AND $h4.@class = "Node" AND $h4.out_Parent.@rid = $h3.@rid
          |  AND $h5.@class = "Camera" AND $h5.out_View.@rid = $h4.@rid
        """.stripMargin
        val finalRow = select(sql).iterator().next()
        assert(23 === finalRow.field[Double]("x"))
        assert(56 === finalRow.field[Double]("y"))
        assert("test2" === finalRow.field[String]("text"))
        assert(1 === finalRow.field[Byte]("t2x"))
        assert(4 === finalRow.field[Byte]("t2y"))
        assert(0 === finalRow.field[Byte]("t3x"))
        assert(0 === finalRow.field[Byte]("t3y"))
        assert(0 === finalRow.field[Byte]("t4x"))
        assert(0 === finalRow.field[Byte]("t4y"))
        assert(0 === finalRow.field[Double]("t5x"))
        assert(0 === finalRow.field[Double]("t5y"))
      }
    }
  }
}