---
weight: 2
---
# Release Notes

---

## Version 1.1.0 M3 (sbt plugin only) (2016-12-20)

Represents the release of the sbt plugin for the Scala debugger. Credit goes
to [@alexarchambault](https://github.com/alexarchambault) for writing
[sbt-ammonite](https://github.com/alexarchambault/sbt-ammonite), which
[@dickwall](https://github.com/dickwall) and I used as a starting point for
writing the sbt plugin for `sdb`.

You can try it out by creating ~/.sbt/0.13/plugins/sdb.sbt:

```
addSbtPlugin("org.scala-debugger" %% "sbt-scala-debugger" % "1.1.0-M3")
```

And then starting `sbt` and running the following command:

```
sdb:run
```

This will start an `sdb` session.

![sbt-sdb](https://cloud.githubusercontent.com/assets/2481802/21404065/10a738f8-c785-11e6-8af2-23d5ac7803fe.gif)

## Version 1.1.0 M3 (2016-12-07)

This is the third iteration of version 1.1.0. This is much closer to the final
representation of the debugger API.

- Includes the first implementation of the Scala debugger language
- Includes the first implementation of the Scala debugger tool (sdb)
- Includes major refactorings of the Scala debugger API, including wrappers
  around JDI event types
- Adds the first build of the debugger API for 2.12 (specifically 2.12.1)

Attached are assembled versions (licenses included inside) of `sdb` compiled
against Scala 2.10, 2.11, and 2.12. The version of Scala used to compile `sdb`
does not influence the debug logic of `sdb`. In other words, you can use `sdb`
compiled against Scala 2.10 to debug code compiled against Scala 2.12.

Instructions on usage will be added to the official website and repository
`docs/` directory. These jars are standalone, meaning that they can be executed
via `java -jar sdb-1.1.0-M3-2.10.jar`, etc.

[sdb 1.1.0 M3 for Scala 2.10](https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.10.jar)
[sdb 1.1.0 M3 for Scala 2.11](https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.11.jar)
[sdb 1.1.0 M3 for Scala 2.12](https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.12.jar)


## Version 1.1.0 M2 (2016-06-21)

This is the second iteration of version 1.1.0. This was cut to provide a couple
of fixes for Ensime server as well as update a couple of APIs that were
determined to be less than ideal. As such, this release is in fact stable, but
is not feature complete. Changes and new features will be documented in the
official 1.1.0 release.

## Version 1.1.0 M1 (2016-05-11)

This is the first iteration of version 1.1.0. This was cut to enable Ensime's
server to have a stable release for the refactored debugger actor code. As
such, this release is in fact stable, but is not feature complete. Changes and
new features will be documented in the official 1.1.0 release.

## Version 1.0.0 (2015-12-23)

- Added high-level Java profile
    - Implemented caching in Java profile traits
- Added high-level swappable profile used by `ScalaVirtualMachine` to
  give the appearance of one API but utilize different APIs underneath
  that can be decided upon programmatically
- Added pipelines to simulate stream of events in same fashion as
  Apache Spark streaming
- Added `JDIArgument` support in low-level and high-level APIs
    - Implemented filters and filter processing for events
    - Implemented data retrieval and data retrieval processing for events
    - Implemented property/filter configuring for requests
- Added dummy implementations of low-level APIs
    - Implemented utilization of dummy APIs to set desired requests
      before virtual machine is started
- Added profile manager to organize available profiles
- Completed missing low-level request management
    - Added low-level access watchpoint request management
    - Added low-level class prepare request management
    - Added low-level class unload request management
    - Added low-level exception request management
    - Added low-level modification watchpoint request management
    - Added low-level monitor contended entered request management
    - Added low-level monitor contended enter request management
    - Added low-level monitor waited request management
    - Added low-level monitor wait request management
    - Added low-level method entry request management
    - Added low-level method exit request management
    - Added low-level thread death request management
    - Added low-level thread start request management
    - Added low-level vm death request management

---

## Version senkbeil (2015-07-11)

!!! note "Note:"
    Released under `org.senkbeil` as version `1.0.0`.

    This was the only version released using the `org.senkbeil` name and is
    incompatible with any other release.

- Added low-level breakpoint creation and event handling
- Added low-level step creation and event handling
- Added wrappers around stack frames, thread references, reference types,
  and values

