# What is the Low-Level API?

---

## Overview

The low-level API is a light abstraction around the JDI that manages the
creation, retrieval, and removal of requests for breakpoints, watchpoints,
monitors, and all other `EventRequest` entities.

The API uses the notion of reactive event handlers that are triggered
whenever a new event occurs, separating handlers by event type (breakpoint,
watchpoint, etc).

## Using event handlers

Event handlers are controlled using the `EventManager` class. Whenever a
request reports an event, all handlers (functions) for the associated event
type are evaluated. Using `addEventHandler` will allow you to specify whether
or not you want the target JVM to resume (if the JVM or thread was suspended
due to the request). Using `addResumingEventHandler` or `addEventStream` will
result in the JVM resuming automatically after evaluating your function(s).

| Method                        | Description                                                                                                                                             |
| ------                        | -----------                                                                                                                                             |
| addEventStream                | Creates a new pipeline that is fed all events of a specified type.                                                                                      |
| addEventStreamWithId          | Same as `addEventStream`, but with the ability to specify the underlying handler's id.                                                                  |
| addEventDataStream            | Creates a new pipeline that is fed all events of a specified type and any collected data specified from extra arguments.                                |
| addEventDataStreamWithId      | Same as `addEventDataStream`, but with the ability to specify the underlying handler's id.                                                              |
| addEventHandler               | Adds a new function to the list that will be called when events of the specified type are received. If returns false, the target JVM remains suspended. |
| addEventHandlerWithId         | Same as `addEventHandler`, but with the ability to specify the id of the event handler.                                                                 |
| addResumingEventHandler       | Same as `addEventHandler`, but the target JVM is always resumed (if the JVM or thread was suspended).                                                   |
| addResumingEventHandlerWithId | Same as `addEventHandlerWithId`, but the target JVM is always resumed (if the JVM or thread was suspended).                                             |
| addEventHandlerFromInfo       | Adds the function from the `EventHandlerInfo` as a new event handler.                                                                                   |


## Example

The following example demonstrates creating a breakpoint using the low-level
`BreakpointManager` and listening for breakpoint events using the
`EventManager`.

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// Create a breakpoint on line 37 of file.scala
// NOTE: Returns a Try[String] where the string is the id that can be used
//       to retrieve or remove the request via the low-level manager
s.lowlevel.breakpointManager.createBreakpointRequest("file.scala", 37)

// NOTE: Unlike high-level APIs, the event handler only passes back
//       generic JDI Event objects that must be cast to the appropriate
//       event type
eventManager.addResumingEventHandler(BreakpointEventType, e => {
  val breakpointEvent = e.asInstanceOf[BreakpointEvent]
  val location = breakpointEvent.location()
  val fileName = location.sourcePath()
  val lineNumber = location.lineNumber()

  println(s"Reached breakpoint: $fileName:$lineNumber")
})
```

## Learn More

Please see the Scaladocs for more information on using individual managers
of the low-level API.

*[JDI]: Java Debugger Interface

