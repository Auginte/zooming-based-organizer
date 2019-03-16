Auginte
=======

Auginte is a tool to augment the meaning
of complex and heterogeneous information 

[![It is like a zooming based mind-map](http://img.youtube.com/vi/ZZ6CZLcWnTE/0.jpg)](http://www.youtube.com/watch?v=ZZ6CZLcWnTE)

Running desktop application
---------------------------

```bash
sbt auginteDesktop/run
```

Assuming you have [sbt](https://www.scala-sbt.org/download.html) v1.2.7+ installed.

Alternatively you can run [Auginte](auginte-desktop/src/main/scala/com/auginte/desktop/Auginte.scala) with `Idea`

Generating distributable binary
-------------------------------

```bash
sbt auginteDesktop/assembly
```
This will generate flat `.jar` somewhere in `auginte-desktop/target/scala-2.12/`

To run the application:
```bash
java -jar auginte-desktop/target/scala-2.12/AuginteDesktop-assembly-0.9.5-SNAPSHOT.jar
```
Where `0.9.5-SNAPSHOT` version may differ

Command line arguments
----------------------

```
auginte.jar [--config-file=...] [SAVED_PROJECT_PATH]
```
If no arguments, using default configuration and example project.

For example:
```bash
java -jar auginte.jar --config-file=auginte-desktop/src/main/resources/example.conf path/to/saved.json
```

Running tests
-------------

Only infinity zoomig is covered with tests
```bash
sbt test
```

Meaning of versions
-------------------

 * `v0.9.5` - Stable version using JSON as a storage
 * `v0.8.*` - added web version: **Maintenance discarded**: Too big memory consumption for simple event-based system
 * `v0.7.*` - OrientDB as a storage: **Maintenance discarded**: incompatible for frequent async search/store
 * `v0.6.4` - JSON based storage: **Retagging as `v0.9.5`**: Too keep application usable and up-to-date
 * `v0.0.*` - `v0.5.*` – [Prototype](https://github.com/Auginte/prototype-desktop-app)

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