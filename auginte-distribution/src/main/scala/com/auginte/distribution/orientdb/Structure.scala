package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.metadata.schema.{OSchema, OType, OClass}
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientGraphNoTx}

/**
 * Creating initial structures to store data in database.
 *
 * {{{
 * Node
 *  ^
 *  : Parent
 *  :
 * Node (x, y)
 *
 * Node
 *  ^
 *  : Inside
 *  :
 * Representation (x, y, scale)
 * }}}
 */
object Structure {
  def createRepository(path: String, connectionType: String = "plocal"): OrientBaseGraph = {
    val database = new OrientGraphNoTx(s"$connectionType:$path")
    val schema = database.getRawGraph.getMetadata.getSchema
    val node = createdNodeVertex(schema)
    val parent = createdParentEdge(schema)
    val representation = createdRepresentationVertex(schema)
    val inside = createdInsideEdge(schema)
    ensureNodeVertexConstrains(node)
    ensureParentEdgeConstrains(parent, node)
    ensureRepresentationVertexConstrains(representation)
    ensureInsideEdgeConstrains(inside, representation, node)
    database
  }

  private def createdNodeVertex(schema: OSchema) = createClass(schema, "Node", schema.getClass("V"))

  private def createdParentEdge(schema: OSchema) = createClass(schema, "Parent", schema.getClass("E"))

  private def createdRepresentationVertex(schema: OSchema) = createClass(schema, "Representation", schema.getClass("V"))

  private def createdInsideEdge(schema: OSchema) = createClass(schema, "Inside", schema.getClass("E"))

  private def createClass(schema: OSchema, name: String, parent: OClass) =
    if (schema.existsClass(name)) schema.getClass(name).setSuperClass(parent)
    else schema.createClass(name, parent)

  private def ensureNodeVertexConstrains(node: OClass): Unit = {
    val positions = List("x", "y")
    removeOldProperties(node, positions)
    positions.map(node.createProperty(_, OType.BYTE)).foreach { property =>
      property.setMandatory(true)
      property.setNotNull(true)
      property.setReadonly(true)
    }
  }

  private def ensureRepresentationVertexConstrains(representation: OClass): Unit = {
    val parameters = List("x", "y", "scale")
    removeOldProperties(representation, parameters)
    parameters.map(representation.createProperty(_, OType.DOUBLE)).foreach { property =>
      property.setMandatory(true)
      property.setNotNull(true)
    }
  }

  private def ensureParentEdgeConstrains(edge: OClass, node: OClass): Unit = {
    removeOldProperties(edge, List("in", "out"))
    edge.createProperty("in", OType.LINKSET, node)
    edge.createProperty("out", OType.LINK, node)
  }

  private def ensureInsideEdgeConstrains(inside: OClass, representation: OClass, node: OClass): Unit = {
    removeOldProperties(inside, List("in", "out"))
    inside.createProperty("in", OType.LINKSET, node)
    inside.createProperty("out", OType.LINK, representation)
  }

  private def removeOldProperties(table: OClass, properties: Seq[String]): Unit =
    properties.filter(table.existsProperty).foreach(table.dropProperty)
}
