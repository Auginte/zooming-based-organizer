package com.auginte.distribution.repository

import java.io.{IOException, OutputStream, InputStream}

import com.auginte.common.SoftwareVersion
import com.auginte.distribution.data._
import com.auginte.distribution.exceptions._
import com.auginte.distribution.json.{BigJson, CommonFormatter, JsonTagEvent, KeysToJsValues}
import com.auginte.transforamtion.{Relation, Descendant}
import com.auginte.zooming.{Grid, IdToRealNode, Node}
import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Simples repository saving everything to JSON files
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class LocalStatic extends Repository {

  val supportedFormatVersion = Version(SoftwareVersion.toString)

  @throws[IOException]
  override def save(output: OutputStream, grid: Grid, elements: Elements, cameras: Cameras): Unit = {
    output.write(saveToString(grid, elements, cameras).getBytes)
  }

  override def load[A <: Descendant, B](
                              input: InputStream,
                              dataFactory: (ImportedData, IdToRealNode) => Option[A],
                              cameraFactory: (ImportedCamera, IdToRealNode) => B):
  (Grid, Seq[Option[A]], Seq[B]) = loadFromStream(input, dataFactory, cameraFactory)

  private[repository] def saveToString(grid: Grid, elements: Elements, cameras: Cameras): String = {

    def description: Description = Description(supportedFormatVersion, elements().size, cameras().size)

    def nodes = grid.flatten

    import com.auginte.distribution.json.CommonFormatter._

    val data = Json.obj(
      "@context" -> Json.toJson("http://auginte.com/ns/v0.6/localStatic.jsonld"),
      "description" -> Json.toJson(description),
      "nodes" -> Json.toJson(nodes),
      "representations" -> Json.toJson(elements()),
      "cameras" -> Json.toJson(cameras())
    )
    Json.stringify(data)
  }

  @throws[ImportException]
  protected[repository] def loadFromStream[A <: Descendant, B](
                            stream: InputStream,
                            dataFactory: (ImportedData, IdToRealNode) => Option[A],
                            cameraFactory: (ImportedCamera, IdToRealNode) => B
                            ): (Grid, Seq[Option[A]], Seq[B]) = {
    import com.auginte.distribution.json.KeysToJsValues.localStorage
    var errors: Option[ImportException] = None
    var nodes: List[ImportedNode] = List()
    val representations = mutable.Map[String, Option[A]]()
    var cameras: List[B] = List()
    lazy val (grid, map) = newGrid.apply(nodes)

    def error(exception: ImportException): Boolean = {
      errors = Some(exception)
      false
    }

    def node(node: ImportedNode): Boolean = {
      nodes = node :: nodes
      true
    }

    def representation(data: Option[A], imported: ImportedData): Boolean = {
      if (data.isDefined) {
        representations.put(imported.id, data)
      }
      true
    }

    def camera(camera: B): Boolean = {
      cameras = camera :: cameras
      true
    }

    reader.read(stream, event => {
      val JsonTagEvent(_, tagName, rawValue) = event
      if (localStorage.contains(tagName)) {
        try {
          val decoded = localStorage(tagName)(rawValue)
          tagName match {
            case "description" => decoded match {
              case Description(version, _, _) if version <= supportedFormatVersion => true
              case Description(version, _, _) => true //error(UnsupportedVersion(version, supportedFormatVersion))
              case e => error(UnsupportedStructure(Description.getClass, e))
            }
            case "nodes" => decoded match {
              case n: ImportedNode => node(n)
              case e => error(UnsupportedStructure(Node.getClass, e))
            }
            case "representations" => decoded match {
              case d: ImportedData => representation(dataFactory(d, map), d)
              case e => error(UnsupportedStructure(ImportedData.getClass, e))
            }
            case "cameras" => decoded match {
              case c: ImportedCamera => camera(cameraFactory(c, map))
              case e => error(UnsupportedStructure(ImportedCamera.getClass, e))
            }
            case o => true // Allow newer data
          }
        } catch {
          case knownException: ImportException => error(knownException)
          case e: Exception => error(UnsupportedElement(e))
        }
      } else {
        error(UnsupportedStructure(getClass, stream))
      }
    })

    representations.values.foreach(r => {
      var connectedSources = List[Relation]()
      r.get.sources.foreach(s => {
        val targetId = s.target.storageId
        if (representations.contains(targetId)) {
          connectedSources = Relation(representations(targetId).get, s.parameters) :: connectedSources
        } else {
          throw new UnconnectedIds(r.get.storageId, targetId)
        }
      })
      r.get.sources = connectedSources
    })

    if (errors.isDefined) throw errors.get
    if (nodes.size < 1) throw UnsupportedStructure(getClass, stream)
    (grid, representations.values.toSeq, cameras)
  }

  private[repository] val reader = new BigJson()

  private[repository] val newGrid = new Grid() {}
}
