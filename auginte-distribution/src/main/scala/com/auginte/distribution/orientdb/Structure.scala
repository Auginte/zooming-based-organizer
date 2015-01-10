package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import com.orientechnologies.orient.core.metadata.schema.{OSchema, OType, OClass}
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientGraphNoTx}

/**
 * Creating initial structures to store data in database.
 *
 * Vertices and edges:
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
 *
 * Type hierarchy:
 * {{{
 *   Representation(x, y, scale)
 *      Text(..., text)
 *      Image(..., path)
 * }}}
 */
object Structure {
  def createRepository(path: String, connectionType: String = "plocal",
                       user: String = "admin", password: String = "admin"): OrientBaseGraph = {
    val database = new OrientGraphNoTx(s"$connectionType:$path", user, password)
    val schema = database.getRawGraph.getMetadata.getSchema
    val node = createdNodeVertex(schema)
    val parent = createdParentEdge(schema)
    val representation = createdRepresentationVertex(schema)
    val inside = createdInsideEdge(schema)
    val vertex = schema.getClass("V")
    val edge = schema.getClass("E")
    val camera = createClass(schema, "Camera", vertex)
    val view = createClass(schema, "View", edge)
    createRawDataVertices(schema)
    ensureNodeVertexConstrains(node)
    ensureParentEdgeConstrains(parent, node)
    ensureRepresentationVertexConstrains(representation)
    ensureInsideEdgeConstrains(inside, representation, node)
    ensureCameraVertexConstrains(camera)
    ensureViewEdgeConstrains(view, camera, node)
    database
  }

  private def createRawDataVertices(schema: OSchema): List[OClass] = {
    val text = createClass(schema, "Text", schema.getClass("Representation"))
    ensureConstraints(text, List("text"), OType.STRING)
    val image = createClass(schema, "Image", schema.getClass("Representation"))
    ensureConstraints(image, List("path"), OType.STRING)
    List(text, image)
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
    ensureConstraints(representation, parameters, OType.DOUBLE)
  }

  private def ensureCameraVertexConstrains(camera: OClass): Unit = {
    val parameters = List("x", "y", "scale")
    ensureConstraints(camera, parameters, OType.DOUBLE)
  }

  private def ensureConstraints(document: OClass, parameters: List[String], fieldType: OType) = {
    removeOldProperties(document, parameters)
    parameters.map(document.createProperty(_, fieldType)).foreach { property =>
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

  private def ensureViewEdgeConstrains(view: OClass, camera: OClass, node: OClass): Unit = {
    removeOldProperties(view, List("in", "out"))
    view.createProperty("in", OType.LINKSET, node)
    view.createProperty("out", OType.LINK, camera)
  }

  private def removeOldProperties(table: OClass, properties: Seq[String]): Unit =
    properties.filter(table.existsProperty).foreach(table.dropProperty)
}
