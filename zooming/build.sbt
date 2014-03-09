initialCommands in console :=
  """
    |import com.auginte.zooming._
    |var scaleFactor = 100
    |def standardSkeleton() = new Skeleton(scaleFactor)
    |def rootSkeletonPair() = {
    |  val skeleton = standardSkeleton()
    |  (skeleton.root, skeleton)
    |}
    |object invalid extends Node(-1, -1) {
    |  override def toString = "Invalid node"
    |  override def equals(obj: scala.Any): Boolean = false
    |}
    |val gridSize = scaleFactor
    |val scaleLog10 = Math.log10(scaleFactor)
    |val (root, skeleton) = rootSkeletonPair()
  """.stripMargin