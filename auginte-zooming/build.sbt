initialCommands in console :=
  """
    |import com.auginte.zooming._
    |var scaleFactor = 100
    |def standardGrid() = new Grid(scaleFactor)
    |def rootGridPair() = {
    |  val grid = standardGrid()
    |  (grid.root, grid)
    |}
    |object invalid extends Node(-1, -1) {
    |  override def toString = "Invalid node"
    |  override def equals(obj: scala.Any): Boolean = false
    |}
    |val gridSize = scaleFactor
    |val scaleLog10 = Math.log10(scaleFactor)
    |val (root, grid) = rootGridPair()
  """.stripMargin