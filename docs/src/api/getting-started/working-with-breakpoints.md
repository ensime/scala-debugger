---
weight: 4
---
# Working with Breakpoints

---

## Overview

The following will provide you with a brief overview of creating breakpoints,
chaining together reactions to breakpoint events, and removing breakpoints.

## Creating

The Scala debugger API provides a simple way to create breakpoints in your
debugger application. Given a _ScalaVirtualMachine_ instance, you can execute
the `getOrCreateBreakpointRequest` method to place a breakpoint on the
specified file and line number.

```scala
val s: ScalaVirtualMachine = /* some virtual machine */
s.getOrCreateBreakpointRequest("some/scala/file.scala", 37)
```

You do not need to worry about creating the same breakpoint over and over
when calling `getOrCreateBreakpontRequest`. This method caches
your request based on the provided arguments. So, you can reference the
same breakpoint multiple times.

!!! note "Note:"
    The file name of the breakpoint must be the ACTUAL file name, not the
    class name. Since Scala allows you to have classes in files whose names
    do not match, you need to make sure that the breakpoint uses the name of
    the file containing the class.

    For example, the class `org.scaladebugger.api.utils.LoopingTask` is
    located in the file `org/scaladebugger/api/utils/LoopingTaskRunner.scala`
    and would use that full file path as the file name of the breakpoint.

## Pipelining

Once you have created a breakpoint, you can chain together a pipeline of
functions that you want to execute when the breakpoint is hit. These functions
are consistent with standard Scala `map`, `flatMap`, and `foreach` structure.

```scala
val s: ScalaVirtualMachine = /* some virtual machine */
s.getOrCreateBreakpointRequest("some/scala/file.scala", 37)
  .foreach(e => println(s"Hit line ${e.lineNumber}!"))
```

## Removing

Whenever you are finished with the breakpoint, you can either remove it
directly or close its pipeline.

### Removing directly

```scala
val s: ScalaVirtualMachine = /* some virtual machine */
val bp = s.getOrCreateBreakpointRequest("some/scala/file.scala", 37)

bp.foreach(_ => println("Hit line 37!"))

// Remove the breakpoint request explicitly (removes all for file and line)
bp.removeBreakpointRequests("some/scala/file.scala", 37)
```

### Removing by closing the pipeline

```scala
val s: ScalaVirtualMachine = /* some virtual machine */
val bp = s.getOrCreateBreakpointRequest("some/scala/file.scala", 37)

bp.foreach(_ => println("Hit line 37!"))

import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
bp.close(data = CloseRemoveAll)
```

By default, `close` will be triggered immediately. Alternatively, you can
invoke `close` with `now = false` to close the pipeline once the next
event occurs.

#### Warning about closing pipelines

Currently, you must provide close with the flag `CloseRemoveAll` in order
to remove the request and any underlying event handlers.

Specifying close without that flag will only stop events going through
that specific pipeline. If all pipelines associated with a request are
closed, the request will also be removed.

## Cookbook

See the [cookbook][cookbook] for a working example.

[cookbook]: /cookbook/creating-a-breakpoint/

