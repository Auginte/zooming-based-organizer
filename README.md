Auginte
=======

Auginte is a tool to augment the meaning
of complex and heterogeneous information 

Development Environment
-----------------------

 * Scala (2.10.3): http://www.scala-lang.org/
 * SBT (0.13.0): http://www.scala-sbt.org/
 * ScalaTest (2.10): http://www.scalatest.org/
 * SCCT (0.2) http://mtkopone.github.io/scct/
 * IntelliJ IDEA (13): http://www.jetbrains.com/idea/

Useful SBT commands
-------------------

```
run
test
assembly
scct:test
gen-idea
project
```

Coding standard
---------------

 * http://docs.scala-lang.org/style/

Known Issues
------------

 * Panel not fills whole window. (Linux, Awesome WM)
   - Solution: `wmname LG3D` before running jar
   - For more: http://www.minecraftforum.net/topic/127416-minecraft-and-awesome-window-manager/
 * After gen-idea, no compiler library found
   - Project settings -> Libraries -> *scala-library* -> Classes: Add from ~/.sbt/boot/*/scala-*
   - Project settings -> Facets -> Compiler library: Choose *scaka-library*

References
------------------

 * http://koleksiuk.github.io/blog/2013/03/29/run-and-setup-scala-with-sbt-plus-scalatest-plus-intellij-idea-12/
 * http://plugins.jetbrains.com/plugin/?idea&id=5007
 * www.cse.unt.edu/~tarau/teaching/SoftEng/scala-swing-design.pdf‎

Author/Copyright
----------------

Aurelijus Banelis
aurelijus@banelis.lt
http://www.auginte.com