---
weight: 0
---
# What is the Profile System?

---

## Overview

The profile system in the Scala debugger API's way of distinguishing specific
logic to handle breakpoints, steps, and other requests in Scala 2.10, Scala
2.11, Scala 2.12, the Scala REPL, Java, and other scenarios.

Currently, there is a profile for Java debugging and another profile
for Scala debugging (targeting 2.10). Future plans include adding logic to
better handle gotchas in Scala 2.10, 2.11, etc.

## Implementation

The `DebugProfile` trait provides a collection of higher-level methods that
can be used to create JDI requests and process incoming events using the
`Pipeline` construct. Each profile implements the `DebugProfile` trait.

The `ScalaVirtualMachine` interface extends the `SwappableDebugProfile` to
enable it to utilize different rules on demand.

![ScalaVirtualMachine][scala-virtual-machine]

## Java Profile

The Java profile is the default profile for the `ScalaVirtualMachine`. It adds
no custom logic for any of the Scala versions; therefore, it should be able to
work with Java code.

![Java Profile Example][java-profile-example]

The Java profile adds support for caching requests, which means calls to
`getOrCreateBreakpointRequest`, `getOrCreateAccessWatchpointRequest`, etc. with
the same arguments will use the same JDI request underneath. This allows you 
to refer to the breakpoint in a more flowing manner like the following:

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// Creates the breakpoint request for the first time, creates a pipeline to
// receive breakpoint events
s.getOrCreateBreakpointRequest("myfile.scala", 37)
  .map(_.location.lineNumber)
  .foreach(l => println(s"Reached line number $l"))

// Reuses the same breakpoint request, creates a pipeline to receive
// breakpoint events
s.getOrCreateBreakpointRequest("myfile.scala", 37)
  .map(_.location.sourcePath)
  .foreach(p => println(s"Breakpoint occurred in $p"))
```

You can specifically reference the profile using its name:

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

import org.scaladebugger.api.profiles.java.JavaDebugProfile
s.withProfile(JavaDebugProfile.Name)
    .getOrCreateBreakpointRequest("myfile.scala", 37)
```


## Scala 2.10 Profile

The Scala 2.10 profile is another profile available to the
`ScalaVirtualMachine`. It adds custom logic targeting Scala 2.10 code, although
the majority of custom rules also work for Scala 2.11 and Scala 2.12.
Furthermore, the profile's rules typically do not affect normal Java debugging,
meaning that this profile can be used to debug a mixed Java/Scala project.

![Scala 2.10 Profile Example][scala-210-profile-example]

Like the Java profile, the Scala 2.10 profile supports caching requests that
use the same arguments. This allows you to refer to the stream for an existing
request.

## Swappable Profile

The swappable profile is a different profile abstraction. Rather than
providing specific debugger logic, this profile allows you to change which
profile you are using automatically. The `ScalaVirtualMachine` class inherits
from the `SwappableDebugProfile` trait, which is why `ScalaVirtualMachine` can
change between different profiles.

| Method             | Description                                           |
| --------           | -----------                                           |
| use                | Changes the current profile to the specified profile. |
| withCurrentProfile | Returns the current underlying profile.               |
| withProfile        | Returns the profile with the specified name.          |

```scala
// Inherits the SwappableDebugProfile trait
val s: ScalaVirtualMachine = /* some virtual machine */

s.use(JavaDebugProfile.Name)

s.getOrCreateBreakpointRequest("file.scala", 37)
```

## Adding your own profile

While default profiles are available in the `ScalaVirtualMachine`,
there may be times where you want/need to provide your own profile. This can be
done by invoking the `register` function on the `ScalaVirtualMachine` class.

```scala
// Inherits the ProfileManager trait
val s: ScalaVirtualMachine = /* some virtual machine */

s.register("profile name", /* profile instance */)
```

[scala-virtual-machine]: /img/api/advanced-topics/scala-virtual-machine.png
[java-profile-example]: /img/api/advanced-topics/java-profile-example.png
[scala-210-profile-example]: /img/api/advanced-topics/scala-210-profile-example.png

*[JDI]: Java Debugger Interface

