package com.auginte.desktop.persistable

import com.auginte.distribution.orientdb
import com.auginte.distribution.orientdb.TestDbHelpers._
import com.auginte.distribution.orientdb._
import com.auginte.zooming
import com.auginte.zooming._
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

/**
 * Helper functions for testing GUI
 */
object TestGuiHelpers {
  def newView(_db: OrientBaseGraph) = {
    orientdb.Node.defaultCache.clear()
    Camera.defaultCache.clear()
    Representation.defaultCache.clear()
    new View {
      grid = new Grid(Position.rootNode(_db)) {
        override private[auginte] def newNode: NodeToNode = {
          case node: orientdb.Node => orientdb.Node(node, _checkParentsConsistency = true)
          case node: zooming.Node => node
        }
      }
      camera = Camera.mainCamera(_db)
    }
  }

  def newText(_view: View, _text: String = "test") = new RepresentationWrapper with ViewWrapper {
    private val _data = new orientdb.Text(_text)

    override def storage: orientdb.Representation = _data

    def cloned: RepresentationWrapper = this.clone().asInstanceOf[this.type]

    view = _view
  }

  def clear(db: OrientBaseGraph) = execSql(db)("DELETE FROM V")
}
