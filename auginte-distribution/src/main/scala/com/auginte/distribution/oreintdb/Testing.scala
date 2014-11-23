package com.auginte.distribution.oreintdb

import java.io.File

import com.orientechnologies.orient.core.command.traverse.OTraverse
import com.orientechnologies.orient.core.command.{OCommandRequest, OCommandResultListener}
import com.orientechnologies.orient.core.db.{ODatabaseRecordThreadLocal, ODatabase}
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.{OSQLSynchQuery, OSQLAsynchQuery}
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientGraphFactory, OrientGraph}
import collection.JavaConversions._
import scala.language.implicitConversions
import java.{lang => jl, util => ju}

object Testing extends App{

  var threads = List[Thread]()

  println(s"Database: $path")
  val connection = s"plocal:$path"
  val graph = createTypes(connection)
  testDocuments(graph)
//  val factory = new OrientGraphFactory(connection).setupPool(1,2)

//  System.setProperty("log.console.level", "FINE")

//  val graph = factory.getTx
  try {
    val luca = graph.addVertex(null, null)
    luca.setProperty("name", "Luca " + System.currentTimeMillis())

    val marko = graph.addVertex(null, null)
    marko.setProperty("name", "Marko" + System.currentTimeMillis())

    val lucaKnowsMarko = marko.addEdge("knows", luca, null, null, "since", "2014-10-15")
    System.out.println("Created edge: " + lucaKnowsMarko.getId)

    for (v <- graph.getVertices) {
      println(s"VERTEXL: ${v.getProperty("name")}")
    }
    for (e <- graph.getEdges) {
      println(s"EDGE: $e")
    }
    graph.commit()

    val query = new OCommandSQL("SELECT FROM v WHERE name = :name")
    val parameters = mapAsJavaMap(Map("name" -> "Luca"))
    for (v <- graph.command(query).execute[jl.Iterable[Vertex]](parameters)) {
      println(s"FOUND: $v")
    }

    testTraverse(graph)
  } catch {
    case e:Exception => println(s"Exception: $e")
  } finally {
    println("Waiting for all threads...")
    threads.foreach(t => t.join())
    graph.shutdown()
  }

  println("Finished")

  private def path = {
    val path = new File("database/db")
    path.mkdirs()
    path.getAbsoluteFile.toString
  }

  private def createTypes(connection: String) = {
    val noTxGraph = new OrientGraphNoTx(connection)
    if (!noTxGraph.getRawGraph.getMetadata.getSchema.existsClass("knows")) {
      noTxGraph.createEdgeType("knows")
      noTxGraph.commit()
    }
    val account = noTxGraph.addVertex("Account", "Account")
    println(s"Accounrt: $account")
    noTxGraph
  }

  private def testDocuments(connection: OrientGraphNoTx): Unit = {
    val database: ODatabaseDocumentTx = connection.getRawGraph
    try {
      val doc = new ODocument("Account")
      doc.field("name", "Test")
      doc.field("time", System.currentTimeMillis())
      doc.save()
      for (d <- ODocuments2Iterator(database.browseClass("Account"))) {
        println(s"D: ${d.getClassName} ${d.field[Int]("time")}")
      }
      val req: OCommandRequest = database.command(
        new OSQLAsynchQuery[ODocument]("SELECT FROM Account WHERE name = 'Test'",
          new OCommandResultListener() {
            private var count = 0

            override def result(record: scala.Any): Boolean = record match {
              case r: ODocument =>
                println(s"Async: ${r.field[Int]("time")}")
                count = count + 1
                println(s"THEAD: ${Thread.currentThread().getName} [RESULT]")
                Thread.sleep(100)
                count < 3
              case _ => false
            }

            override def end(): Unit = {
              println("END")
            }
          }))
      val later = new Thread(new Runnable {
        override def run(): Unit = {
          ODatabaseRecordThreadLocal.INSTANCE.set(database)
          req.execute()
        }
      })
      println(s"THEAD: ${Thread.currentThread().getName} [MAIN]")
      threads = later :: threads
      later.start()
    } catch {
      case e: Exception => println(e)
    }
  }

  private def testTraverse(connection: OrientGraphNoTx): Unit = {
    val tr = new OSQLSynchQuery[ODocument]("SELECT @class, @rid, $path FROM (TRAVERSE in, out FROM V WHILE $depth <= 2)")
    for (t <- OSynchQUery2Iterator(tr)) {
      println(s"TRAVERSE ${t.field("class")}: ${t.field("rid")} \t\t => ${t.field("$path")}")
    }
  }

  implicit def ODocuments2Iterator(l :ORecordIteratorClass[ODocument]) = new Iterable[ODocument] {
    override def iterator = new Iterator[ODocument] {
      override def hasNext: Boolean = l.hasNext

      override def next(): ODocument = l.next()
    }
  }

  implicit def OSynchQUery2Iterator(l :OSQLSynchQuery[ODocument]) = new Iterable[ODocument] {
    override def iterator = l.iterator()
  }
}
