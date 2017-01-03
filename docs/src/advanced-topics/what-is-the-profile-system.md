# What is the Profile System?

---

## Overview

The profile system in the Scala debugger API's way of distinguishing specific
logic to handle breakpoints, steps, and other requests in Scala 2.10, Scala
2.11, Scala 2.12, the Scala REPL, Java, and other scenarios.

Currently, there are no specific profiles for Scala versions. Future plans
include adding logic to better handle closures in Scala 2.10, 2.11, etc.

## Available Functionality

The `DebugProfile` trait provides a collection of higher-level methods that
can be used to create JDI requests and process incoming events using the
`Pipeline` construct. Each profile implements the `DebugProfile` trait.

### Breakpoints

| Method                     | Description                                                                                                                                                                                                                       |
| ------                     | -----------                                                                                                                                                                                                                       |
| onBreakpoint               | Attempts to create a breakpoint with the provided file and line number, returning a Try containing a pipeline of breakpoint events.                                                                                               |
| onBreakpointWithData       | Attempts to create a breakpoint with the provided file and line number, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeBreakpoint         | Same as `onBreakpoint`, but throws an exception if an error occurs.                                                                                                                                                               |
| onUnsafeBreakpointWithData | Same as `onBreakpointWithData`, but throws an exception if an error occurs.                                                                                                                                                       |

### Classes

#### Class Loading

| Method                       | Description                                                                                                                                                                                           |
| ------                       | -----------                                                                                                                                                                                           |
| onClassPrepare               | Attempts to create a class prepare request (to receive an event whenever a class is loaded into the target JVM), returning a Try containing a pipeline of class prepare events.                       |
| onClassPrepareWithData       | Attempts to create a class prepare request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeClassPrepare         | Same as `onClassPrepare`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeClassPrepareWithData | Same as `onClassPrepareWithData`, but throws an exception if an error occurs.                                                                                                                         |

!!! note "Note:"
    The standard `ScalaVirtualMachine` already creates this request __without__
    any extra arguments; so, use of `onClassPrepare` will already be cached.

#### Class Unloading

| Method                       | Description                                                                                                                                                                                           |
| ------                       | -----------                                                                                                                                                                                           |
| onClassUnload               | Attempts to create a class unload request (to receive an event whenever a class is unloaded from the target JVM), returning a Try containing a pipeline of class unload events.                       |
| onClassUnloadWithData       | Attempts to create a class unload request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeClassUnload         | Same as `onClassUnload`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeClassUnloadWithData | Same as `onClassUnloadWithData`, but throws an exception if an error occurs.                                                                                                                         |

### Event-only Listening

| Method                | Description                                                                                                                                                                                    |
| ------                | -----------                                                                                                                                                                                    |
| onEvent               | Attempts to create a resuming event handler for the specified event type that feeds a pipeline of events for the specified event type.                                                         |
| onEventWithData       | Attempts to create an event handler, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeEvent         | Same as `onEvent`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeEventWithData | Same as `onEventWithData`, but throws an exception if an error occurs.                                                                                                                         |

!!! warning "Warning:"
    There is currently no caching of event handler creation; so, each call to
    `onEvent` or its associated functions will create a new underlying event
    handler.

### Exceptions

#### Specific Exceptions

| Method                    | Description                                                                                                                                                                                       |
| ------                    | -----------                                                                                                                                                                                       |
| onException               | Attempts to create a exception request (to receive an event whenever the specified exception or subclass is thrown), returning a Try containing a pipeline of exception events.                   |
| onExceptionWithData       | Attempts to create a exception request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeException         | Same as `onException`, but throws an exception if an error occurs.                                                                                                                                |
| onUnsafeExceptionWithData | Same as `onExceptionWithData`, but throws an exception if an error occurs.                                                                                                                        |

#### All Exceptions

| Method                        | Description                                                                                                                                                                                       |
| ------                        | -----------                                                                                                                                                                                       |
| onAllExceptions               | Attempts to create a exception request (to receive an event whenever any exception is thrown), returning a Try containing a pipeline of exception events.                                         |
| onAllExceptionsWithData       | Attempts to create a exception request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeAllExceptions         | Same as `onAllExceptions`, but throws an exception if an error occurs.                                                                                                                            |
| onUnsafeAllExceptionsWithData | Same as `onAllExceptionsWithData`, but throws an exception if an error occurs.                                                                                                                    |


### Info

| Method                | Description                                                                                  |
| ------                | -----------                                                                                  |
| availableLinesForFile | Retrieves the line numbers that are breakpointable in the target JVM for the specified file. |
| mainClassName         | Retrieves the name of the class that served as the entrypoint for the target JVM.            |
| commandLineArguments  | Retrieves the collection of arguments provided to the target JVM at startup.                 |

### Methods

#### Method Entry

| Method                      | Description                                                                                                                                                                                          |
| ------                      | -----------                                                                                                                                                                                          |
| onMethodEntry               | Attempts to create a method entry request using the provided class and method name, returning a Try containing a pipeline of method entry events.                                                    |
| onMethodEntryWithData       | Attempts to create a method entry request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMethodEntry         | Same as `onMethodEntry`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeMethodEntryWithData | Same as `onMethodEntryWithData`, but throws an exception if an error occurs.                                                                                                                         |

#### Method Exit

| Method                     | Description                                                                                                                                                                                         |
| ------                     | -----------                                                                                                                                                                                         |
| onMethodExit               | Attempts to create a method exit request using the provided class and method name, returning a Try containing a pipeline of method exit events.                                                     |
| onMethodExitWithData       | Attempts to create a method exit request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMethodExit         | Same as `onMethodExit`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeMethodExitWithData | Same as `onMethodExitWithData`, but throws an exception if an error occurs.                                                                                                                         |

### Monitors

#### Monitor Contended Entered

| Method                                  | Description                                                                                                                                                                                                       |
| ------                                  | -----------                                                                                                                                                                                                       |
| onMonitorContendedEntered               | Attempts to create a monitor contended entered request, returning a Try containing a pipeline of monitor contended entered events.                                                                                |
| onMonitorContendedEnteredWithData       | Attempts to create a monitor contended entered request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMonitorContendedEntered         | Same as `onMonitorContendedEntered`, but throws an exception if an error occurs.                                                                                                                                  |
| onUnsafeMonitorContendedEnteredWithData | Same as `onMonitorContendedEnteredWithData`, but throws an exception if an error occurs.                                                                                                                          |

#### Monitor Contended Enter

| Method                                | Description                                                                                                                                                                                                     |
| ------                                | -----------                                                                                                                                                                                                     |
| onMonitorContendedEnter               | Attempts to create a monitor contended enter request, returning a Try containing a pipeline of monitor contended enter events.                                                                                  |
| onMonitorContendedEnterWithData       | Attempts to create a monitor contended enter request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMonitorContendedEnter         | Same as `onMonitorContendedEnter`, but throws an exception if an error occurs.                                                                                                                                  |
| onUnsafeMonitorContendedEnterWithData | Same as `onMonitorContendedEnterWithData`, but throws an exception if an error occurs.                                                                                                                          |

#### Monitor Waited

| Method                        | Description                                                                                                                                                                                            |
| ------                        | -----------                                                                                                                                                                                            |
| onMonitorWaited               | Attempts to create a monitor waited request, returning a Try containing a pipeline of monitor waited events.                                                                                           |
| onMonitorWaitedWithData       | Attempts to create a monitor waited request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMonitorWaited         | Same as `onMonitorWaited`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeMonitorWaitedWithData | Same as `onMonitorWaitedWithData`, but throws an exception if an error occurs.                                                                                                                         |

#### Monitor Wait

| Method                      | Description                                                                                                                                                                                          |
| ------                      | -----------                                                                                                                                                                                          |
| onMonitorWait               | Attempts to create a monitor wait request, returning a Try containing a pipeline of monitor wait events.                                                                                             |
| onMonitorWaitWithData       | Attempts to create a monitor wait request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeMonitorWait         | Same as `onMonitorWait`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeMonitorWaitWithData | Same as `onMonitorWaitWithData`, but throws an exception if an error occurs.                                                                                                                         |

### Steps

#### Line-specific steps

| Method               | Description                                                                                                                                                                                       |
| ------               | -----------                                                                                                                                                                                       |
| stepIntoLine         | Attempts to create a step request to step into the next line of code, returning a future with the result.                                                                                         |
| stepIntoLineWithData | Attempts to create a step request to step into the next line of code, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments.   |
| stepOverLine         | Attempts to create a step request to step over the next line of code, returning a future with the result.                                                                                         |
| stepOverLineWithData | Attempts to create a step request to step over the next line of code, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments.   |
| stepOutLine          | Attempts to create a step request to step out of the next line of code, returning a future with the result.                                                                                       |
| stepOutLineWithData  | Attempts to create a step request to step out of the next line of code, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments. |

#### Next available location steps

| Method              | Description                                                                                                                                                                                   |
| ------              | -----------                                                                                                                                                                                   |
| stepIntoMin         | Attempts to create a step request to step into the next location, returning a future with the result.                                                                                         |
| stepIntoMinWithData | Attempts to create a step request to step into the next location, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments.   |
| stepOverMin         | Attempts to create a step request to step over the next location, returning a future with the result.                                                                                         |
| stepOverMinWithData | Attempts to create a step request to step over the next location, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments.   |
| stepOutMin          | Attempts to create a step request to step out of the next location, returning a future with the result.                                                                                       |
| stepOutMinWithData  | Attempts to create a step request to step out of the next location, returning a future with a tuple of (event, collection of data) where data is collected based on provided extra arguments. |

#### Event-only listening

| Method               | Description                                                                                                                                                                                                           |
| ------               | -----------                                                                                                                                                                                                           |
| onStep               | Attempts to create a resuming event handler for the step events that feeds a pipeline of step events.                                                                                                                 |
| onStepWithData       | Attempts to create a resumingevent handler for step events, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeStep         | Same as `onStep`, but throws an exception if an error occurs.                                                                                                                                                         |
| onUnsafeStepWithData | Same as `onStepWithData`, but throws an exception if an error occurs.                                                                                                                                                 |

!!! warning "Warning:"
    There is currently no caching of event handler creation; so, each call to
    `onStep` or its associated functions will create a new underlying event
    handler.

### Threads

#### Thread Death

| Method                      | Description                                                                                                                                                                                          |
| ------                      | -----------                                                                                                                                                                                          |
| onThreadDeath               | Attempts to create a thread death request, returning a Try containing a pipeline of thread death events.                                                                                             |
| onThreadDeathWithData       | Attempts to create a thread death request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeThreadDeath         | Same as `onThreadDeath`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeThreadDeathWithData | Same as `onThreadDeathWithData`, but throws an exception if an error occurs.                                                                                                                         |

#### Thread Start

| Method                      | Description                                                                                                                                                                                          |
| ------                      | -----------                                                                                                                                                                                          |
| onThreadStart               | Attempts to create a thread start request, returning a Try containing a pipeline of thread start events.                                                                                             |
| onThreadStartWithData       | Attempts to create a thread start request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeThreadStart         | Same as `onThreadStart`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeThreadStartWithData | Same as `onThreadStartWithData`, but throws an exception if an error occurs.                                                                                                                         |

### Virtual Machine Status

#### VM Start

| Method                  | Description                                                                                                                                                                                                        |
| ------                  | -----------                                                                                                                                                                                                        |
| onVMStart               | Attempts to create an event handler for vm start events, returning a Try containing a pipeline of vm start events.                                                                                                 |
| onVMStartWithData       | Attempts to create an event handler for vm start events, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeVMStart         | Same as `onVMStart`, but throws an exception if an error occurs.                                                                                                                                                   |
| onUnsafeVMStartWithData | Same as `onVMStartWithData`, but throws an exception if an error occurs.                                                                                                                                           |

#### VM Disconnect

| Method                       | Description                                                                                                                                                                                                             |
| ------                       | -----------                                                                                                                                                                                                             |
| onVMDisconnect               | Attempts to create an event handler for vm disconnect events, returning a Try containing a pipeline of vm disconnect events.                                                                                            |
| onVMDisconnectWithData       | Attempts to create an event handler for vm disconnect events, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeVMDisconnect         | Same as `onVMDisconnect`, but throws an exception if an error occurs.                                                                                                                                                   |
| onUnsafeVMDisconnectWithData | Same as `onVMDisconnectWithData`, but throws an exception if an error occurs.                                                                                                                                           |

#### VM Death

| Method                  | Description                                                                                                                                                                                      |
| ------                  | -----------                                                                                                                                                                                      |
| onVMDeath               | Attempts to create a vm death request, returning a Try containing a pipeline of vm death events.                                                                                                 |
| onVMDeathWithData       | Attempts to create a vm death request, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeVMDeath         | Same as `onVMDeath`, but throws an exception if an error occurs.                                                                                                                                 |
| onUnsafeVMDeathWithData | Same as `onVMDeathWithData`, but throws an exception if an error occurs.                                                                                                                         |

### Watchpoints

#### Access Watchpoint

| Method                           | Description                                                                                                                                                                                                                              |
| ------                           | -----------                                                                                                                                                                                                                              |
| onAccessWatchpoint               | Attempts to create a access watchpoint request for the specified class' field, returning a Try containing a pipeline of access watchpoint events.                                                                                        |
| onAccessWatchpointWithData       | Attempts to create a access watchpoint request for the specified class' field, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeAccessWatchpoint         | Same as `onAccessWatchpoint`, but throws an exception if an error occurs.                                                                                                                                                                |
| onUnsafeAccessWatchpointWithData | Same as `onAccessWatchpointWithData`, but throws an exception if an error occurs.                                                                                                                                                        |

#### Modification Watchpoint

| Method                                 | Description                                                                                                                                                                                                                                    |
| ------                                 | -----------                                                                                                                                                                                                                                    |
| onModificationWatchpoint               | Attempts to create a modification watchpoint request for the specified class' field, returning a Try containing a pipeline of modification watchpoint events.                                                                                  |
| onModificationWatchpointWithData       | Attempts to create a modification watchpoint request for the specified class' field, returning a Try containing a pipeline of tuples in the form of (event, collection of data) where the data is collected based on provided extra arguments. |
| onUnsafeModificationWatchpoint         | Same as `onModificationWatchpoint`, but throws an exception if an error occurs.                                                                                                                                                                |
| onUnsafeModificationWatchpointWithData | Same as `onModificationWatchpointWithData`, but throws an exception if an error occurs.                                                                                                                                                        |

## Pure Profile

The pure profile is the default profile for the `ScalaVirtualMachine`. It adds
no custom logic for any of the Scala versions; therefore, it should be able to
work with Java code.

The pure profile adds support for caching requests, which means calls to
`onBreakpoint`, `onUnsafeBreakpoint`, etc. with the same arguments will use the
same JDI request underneath. This allows you to refer to the breakpoint in a
more flowing manner like the following:

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// Creates the breakpoint request for the first time, creates a pipeline to
// receive breakpoint events
s.onBreakpoint("myfile.scala", 37)
  .map(_.location().lineNumber())
  .foreach(l => println(s"Reached line number $l"))

// Reuses the same breakpoint request, creates a pipeline to receive
// breakpoint events
s.onBreakpoint("myfile.scala", 37)
  .map(_.location().sourcePath())
  .foreach(p => println(s"Breakpoint occurred in $p"))
```

You can specifically reference the profile using its name:

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// Use the ScalaVirtualMachine with the pure profile
import org.scaladebugger.api.profiles.pure.PureDebugProfile
s.withProfile(PureDebugProfile.Name).onBreakpoint("myfile.scala", 37)
```

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

// All future calls to the profile will route to the pure debug profile
s.use(PureDebugProfile.Name)

// This will be handled by the pure debug profile
s.onBreakpoint("file.scala", 37)
```

## Adding your own profile

While default profiles are available in the `ScalaVirtualMachine` by default,
there may be times where you want/need to provide your own profile. This can be
done by invoking the `register` function on the `ScalaVirtualMachine` class.

```scala
// Inherits the ProfileManager trait
val s: ScalaVirtualMachine = /* some virtual machine */

s.register("profile name", /* profile instance */)
```

*[JDI]: Java Debugger Interface

