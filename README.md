Simple scalaJs example
======================

Useful commands
---------------

```
~fastOptJS
test
fullOptJS
```

Will generate:

* `index-fastopt.html`
* `index.html`

`scalacOptions ++= Seq("-feature")` for warnings.

For debugging:

```
import scala.scalajs.js.Dynamic.{ global => g }

g.console.log(event)
```

GUI functionality
-----------------

* Add new elements
* Move elements with mouse or touch (swipe)
* Move camera with mouse or touch (swipe)
* Zoom with mouse wheel or touch (zoom)

Design decisions
----------------

* While dragging mouse up can no longer be on same (owned) element - position changes are passed to parent
* Passing callback/proxy object to owned components.
* Putting state copy functions to state, leaving backend for higher abstraction
* Dragging in touch screens are disabled, as devises are too slow to update DOM and calculations messes up

Author
------

Aurelijus Banelis <aurelijus@banelis.lt>