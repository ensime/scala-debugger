---
weight: 3
---
# Making Requests Early

---

## Overview

In some situations, you might want or need to create JDI requests before the
debugger has connected to the target JVM. To do this, you can take advantage
of the `DummyScalaVirtualMachine` to use the same API to create and manage
your requests and events __before__ the target JVM starts.

Whenever you call a function on a `DummyScalaVirtualMachine`, the underlying
request is placed on a pending queue. When you provide the dummy VM to a
debugger, all of the pending requests are applied to each JVM that connects
using that debugger.

## Creating the dummy VM

To create a `DummyScalaVirtualMachine` that delegates all functions to be
performed when a real `ScalaVirtualMachine` connects, you can use the
companion object's helper method:

```scala
val d: DummyScalaVirtualMachine = DummyScalaVirtualMachine.newInstance()
```

## Using the dummy VM with debuggers

When you have a `DummyScalaVirtualMachine` that you would like to apply to a
debugger, you can add it using `addPendingScalaVirtualMachine` or
`withPending` on the debugger.

```scala
val debugger: Debugger = /* some debugger instance */
val d: DummyScalaVirtualMachine = /* some dummy virtual machine */

// Both operations perform the same task
debugger.addPendingScalaVirtualMachine(d)
debugger.withPending(d)
```

## Limitations of the dummy VM

Currently, there are a couple of limitations when using the
`DummyScalaVirtualMachine` to prepare requests.

1. You cannot nest creation of event handlers within the callbacks of
   other event handlers or pipelines.

        val d: DummyScalaVirtualMachine = /* some dummy virtual machine */

        // Will get evaluated
        d.getOrCreateBreakpointRequest("file.scala", 37)

        // Will get evaluated
        d.createEventListener(BreakpointEventType, be => {
          d.lowlevel.addResumingEventHandler(StepEventType, e => {
            // Will never get evaluated
          })
          
          d.stepOverLine(be.thread)
        })

2. You cannot nest creation of pipelines within the callbacks of other
   event handlers or pipelines.

        val d: DummyScalaVirtualMachine = /* some dummy virtual machine */

        // Will get evaluated
        d.getOrCreateBreakpointRequest("file.scala", 37).foreach(e => {
          d.stepOverLine(e.thread).foreach(stepEvent => {
            // Will never get evaluated
          })
        })

3. You cannot invoke `availableLinesForFile` as the target JVM needs to be
   loaded to determine that information.

4. You cannot invoke `mainClassName` as the target JVM needs to be loaded to
   determine that information.

5. You cannot invoke `commandLineArguments` as the target JVM needs to be
   loaded to determine that information.

6. Any invocation on the dummy VM after the target JVM connects will not
   be applied.

*[JDI]: Java Debugger Interface

