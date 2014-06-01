package com.auginte.desktop

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.control.{TreeView, Label, Button}
import scalafx.scene.layout.{HBox, BorderPane}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.application.Platform
import akka.actor.{Props, ActorSystem}
import scalafx.stage.WindowEvent
import com.auginte.desktop.{actors => act}
import com.auginte.desktop.zooming.Grid
import javafx.scene.control._
import com.auginte.desktop.nodes.MapRow
import javafx.scene.control.cell.PropertyValueFactory
import scala.Some
import com.auginte.zooming.Node
import javafx.scene.{Node => jn}
import com.auginte.zooming.{Node => zn}
import scalafx.event.Event
import javafx.util.Callback
import javafx.{scene => jfxs}
import javafx.scene.{control => jfxc}
import javafx.scene.shape.Rectangle

object HelloScalaFX extends JFXApp {
  val akka = ActorSystem("auginte")

  stage = new PrimaryStage {
    title = "Auginte"
    width = 600
    height = 450
  }

  val view1 = new View {
    prefWidth = 400.0
    prefHeight = 400.0
  }
  view1.stylesheets add "css/view.css"
  view1.stylesheets add "css/controls.css"
  view1.styleClass.add("view")

  val exitButton = new Button("Exit") {
    onAction = (e: ActionEvent) => quit()
  }

  val mapTable = new TableView[MapRow]()
  debugRelation()

  val grid = new Grid
  val viewsSupervisor = akka.actorOf(Props[act.Views], "views")
  viewsSupervisor ! grid
  viewsSupervisor ! view1
  stage.onCloseRequest = ((e: WindowEvent) => quit())

  val hierarchyTree = new TreeView[String]()
  val allHierarchyTree = new TreeView[String]()

  def quit(): Unit = {
    akka.shutdown()
    Platform.exit()
  }


  val tabPane = new TabPane()
  initTabs()

  val infoLabel = new Label("Double-click to add new element/edit. Enter to finish.\n" +
    "Right click for context menu. Shift enter for new line.")

  stage.scene = new Scene {
    root = new BorderPane {
      center = tabPane
      bottom = new HBox {
        content = List(infoLabel, exitButton)
        spacing = 5
      }
    }
  }

  def initTabs(): Unit = {
    val view1Tab = new Tab("Zooming")
    view1Tab.setContent(view1)
    tabPane.getTabs().add(view1Tab)

    val mapTab = new Tab("Mapping")
    mapTab.setContent(mapTable)
    tabPane.getTabs().add(mapTab)

    val hierarchyTab = new Tab("Hierarchy")
    hierarchyTab.setContent(hierarchyTree)
    hierarchyTab.setOnSelectionChanged((e: Event) => debugHierarchy(hierarchyTree))
    tabPane.getTabs().add(hierarchyTab)

    val allTab = new Tab("All")
    allTab.setContent(allHierarchyTree)
    allTab.setOnSelectionChanged((e: Event) => debugHierarchyWithElements(allHierarchyTree))
    tabPane.getTabs().add(allTab)
  }

  def debugRelation(): Unit = {
    val guiColumn = new TableColumn[MapRow, String]("GUI")
    guiColumn.setCellValueFactory(new PropertyValueFactory[MapRow, String]("key"))
    guiColumn.setPrefWidth(100)
    val gridColumn = new TableColumn[MapRow, String]("Grid")
    gridColumn.setCellValueFactory(new PropertyValueFactory[MapRow, String]("value"))
    gridColumn.setPrefWidth(300)
    mapTable.getColumns.addAll(guiColumn, gridColumn)
    view1.mapTable = Some(mapTable)
  }

  def debugHierarchy(tree: TreeView[String]): Unit = {
    def nodesToItems(item: TreeItem[String], node: Node): Unit = for (subNode <- node) {
      val subItem = new TreeItem[String](subNode.toString)
      item.getChildren.add(subItem)
      item.setExpanded(true)
      if (subNode == view1.node) {
        val cameraNode = new TreeItem[String]("Camera: " + view1.toString())
        subItem.getChildren.add(cameraNode)
        subItem.setExpanded(true)
      }
      if (subNode.size > 0) nodesToItems(subItem, subNode)
    }

    tree.root = new TreeItem[String](grid.root.toString)
    tree.root.value.setExpanded(true)
    nodesToItems(tree.root.value, grid.root)
  }

  def debugHierarchyWithElements(tree: TreeView[String]): Unit = {
    def nodesAndElementsToItems(item: TreeItem[String], node: Node): Unit = {
      val map = grid.map
      val elements = view1.delegate.getChildren
      for (e <- elements; if map.contains(e) && map.get(e).get == node) {
        val subItem = new TreeItem[String](e.toString)
        item.getChildren.add(subItem)
        item.setExpanded(true)
      }
      for (subNode <- node) {
        val subItem = new TreeItem[String](subNode.toString)
        item.getChildren.add(subItem)
        item.setExpanded(true)
        nodesAndElementsToItems(subItem, subNode)
      }
    }

    tree.root = new TreeItem[String](grid.root.toString)
    tree.root.value.setExpanded(true)
    nodesAndElementsToItems(tree.root.value, grid.root)
  }
}