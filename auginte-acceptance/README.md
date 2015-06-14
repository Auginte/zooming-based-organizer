Acceptance tests for Auginte
============================

Running test
-------------

```sh
sbt test -Durl="http://wapsite.lt" -DwebDriver=chrome -DdriverUrl="http://127.0.0.1:9515" -DchromePath="/usr/bin/chromium-browser"
```

Or from code
```scala
sys.props += "webDriver" -> "chrome"
sys.props += "url" -> "http://demo.auginte.com"
```

Troubleshooting
---------------

If running with `chrome`, check if `chromium-browser` is running