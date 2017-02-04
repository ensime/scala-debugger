# Scala Debugger Docs

To generate and serve the docs, run the following:

```
sbt 'scalaDebuggerDocs/run -gs --allow-unsupported-media-types'
```

Note the use of `--allow-unsupported-media-types`, which is needed to serve
fonts bundled with the _font-awesome_ library.
