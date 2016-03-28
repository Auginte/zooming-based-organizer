Zooming based organiser
=======================

[![Build Status](https://secure.travis-ci.org/Auginte/zooming-based-organizer.png?branch=master)](http://travis-ci.org/Auginte/zooming-based-organizer)
[![Build Status Develop](https://secure.travis-ci.org/Auginte/zooming-based-organizer.png?branch=develop)](http://travis-ci.org/Auginte/zooming-based-organizer)

This is one of major [Auginte tools](http://auginte.com):
Desktop and web application to help brainstorming, grouping and analysis of complex and heterogeneous information.

Desktop and web application can be used autonomously, it was put together just to reuse common code easier.

[![It is like a zooming based mind-map](http://img.youtube.com/vi/ZZ6CZLcWnTE/0.jpg)](http://www.youtube.com/watch?v=ZZ6CZLcWnTE)

Installing
==========

**tldr**: Download from [releases](https://github.com/Auginte/zooming-based-organizer/releases), run with `java -jar`

See [INSTALL.md](INSTALL.md) for more examples 

Stability
=========

This is **not** production ready!
For a long time, this project was developed as a personal and/or closed source software,
so it was not designed for easy set-up and easy contributing.

**Still working on improving that**.
 
Current functionality and long term goals
---------------

* (Done) **Infinity zooming** - helps brainstorming and analysis by zooming in and out between details and essence
  * Fully functional and covered with unit tests
* (Partly) **Source tracking** - should help use clues of larger information for faster analysis/perception, while still
  being able to find context. Common example: some formula and long description of all the letters and operations
  * Have primitive internal linking and visualisation of internal links (holding `Shift` key)
  * Does not have links to externals soruces: WebSites, PDF pages, Conferences, etc
* (Not started) **Distributed architecture** - this is part of *Knowledge economy* functionality -
  to augment/track information, instead of copy-pasting it.
   * Infrastructure is being developed separately. This software will be like a client. 

Development Environment
-----------------------

 * Scala (2.10.2): http://www.scala-lang.org/
 * SBT (0.13.0): http://www.scala-sbt.org/
 * ScalaTest (2.10): http://www.scalatest.org/
 * SCCT (0.2) http://mtkopone.github.io/scct/
 * IntelliJ IDEA (14): http://www.jetbrains.com/idea/

For JavaFx, `JAVA_HOME` must be available as environment variable. E.g.

```
    export JAVA_HOME=/usr/lib/jvm/java-7-oracle
```

Useful SBT commands
-------------------

```
run
test
assembly
project
fastOptJS
fullOptJS
```

This is multi-project build.


Known Issues
------------

 * Panel not fills whole window. (Linux, Awesome WM)
   - Solution: `wmname LG3D` before running jar
   - For more: http://www.minecraftforum.net/topic/127416-minecraft-and-awesome-window-manager/
 * After gen-idea, no compiler library found
   - Project settings -> Libraries -> *scala-library* -> Classes: Add from ~/.sbt/boot/*/scala-*
   - Project settings -> Facets -> Compiler library: Choose *scaka-library*
 * `JavaFx` runtime is only in `Oracle JRE 1.7+`, not in OpenJRE.
   - Internet is full of [examples](https://www.digitalocean.com/community/tutorials/how-to-install-java-on-ubuntu-with-apt-get)
     how to install Oracle Java and make it as default.

Useful links (for developers)
-----------------------------

 * http://koleksiuk.github.io/blog/2013/03/29/run-and-setup-scala-with-sbt-plus-scalatest-plus-intellij-idea-12/
 * http://plugins.jetbrains.com/plugin/?idea&id=5007
 * www.cse.unt.edu/~tarau/teaching/SoftEng/scala-swing-design.pdf‎
 * http://docs.scala-lang.org/style/
 * http://docs.openstack.org/infra/jenkins-job-builder/publishers.html

License
-------

[Apache 2.0](LICENSE)

Author
------

Aurelijus Banelis
http://auginte.com