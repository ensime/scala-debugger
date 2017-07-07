---
weight: 2
---
# Available Debugger Connections

In this section, you will get a brief overview of the available options for
connecting a debugger programmatically to one or more target JVMs.

---

## Overview

When you want to debug a JVM, you need to establish some form of communication
between the target JVM, the one you want to debug, and the debugger, the
JVM used to perform the debugging. When using the Java Debugger Interface,
there is an abstraction called a [connector][connectors] that is used when
establishing the connection between a debugger application (using JDI) and the
target JVM.

The two common means of transport are socket-based and memory-based. As the
shared memory transport is limited to Windows, all of the connectors available
in the Scala debugger API use connectors that use socket transportation.

The three most common ways to establish a connection between the debugger and
a target JVM are to launch the target JVM and connect to it, to attach to an
already-running JVM, or to listen for connections from target JVMs.

Below, you will read about these three methods of starting communication with
a target JVM and how you can perform these using the Scala debugger API.

## Launching Debugger

The launching debugger is the commonly-used method in IDEs like Scala IDE and
IntelliJ when you have a program that you both want to start and debug.

### Instantiating

To create the launching debugger using the Scala debugger API, you instantiate
a new instance of the debugger using the companion object:

```scala
val launchingDebugger = LaunchingDebugger(className = "some.class.name")
```

The only required argument for the launching debugger is the fully-qualified
class name that you want to use as the entrypoint for the target JVM. The
current classpath of the debugger is used when launching the target JVM; so,
the specified class must exist in your current classpath.

The launching debugger can also take additional arguments including command
line options to pass to the main method, jvm options to provide to the target
JVM when started, and a flag indicating whether or not to suspend the target
JVM until the debugger has connected to it.

### Starting

When you are ready to launch the target JVM using the provided class as the
entrypoint to the target JVM, call the `start` method.

```scala
launchingDebugger.start { s =>
  println("Launched and connected to JVM: " + s.uniqueId)
}
```

When you call `start` with the launching debugger, you provide a function that
takes a `ScalaVirtualMachine` as an argument. This is treated as a callback
that is invoked when the launching debugger connects to the started target JVM
process.

### Stopping

To stop the launching debugger, you can call the `stop` function:

```scala
launchingDebugger.stop()
```

This kills the target JVM process and stops the debugger.

### Cookbook

The `ScalaVirtualMachine` contains the majority of debugger-related functions
that you can use to invoke JDI operations.

See the [cookbook][cookbook-launching] for a full example.

## Attaching Debugger

The attaching debugger is the next most-used method in IDEs like Scala IDE and
IntelliJ when you have the source code for the target JVM loaded up but want to
connect to an already-running JVM process.

It begins with the target JVM already running with the necessary JDWP arguments
to expose a TCP port that the debugger will use to connect (attach):

```
# Allow debuggers to attach via port 5005
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

### Instantiating

To create the attaching debugger using the Scala debugger API, you instantiate
a new instance of the debugger using the companion object:

```scala
val attachingDebugger = AttachingDebugger(port = 5005)
```

The only required argument for the attaching debugger is the port that the
debugger will use when connecting to the target JVM. This port should match the
port you provided in the JDWP arguments to the target JVM when starting it on
your own.

The attaching debugger can also take additional arguments including the hostname
used when connecting to the target JVM (defaulting to localhost) and the maximum
time to wait (in milliseconds) for a successful connection before timing out.

### Starting

When you are ready to attach to the target JVM, call the `start` method.

```scala
attachingDebugger.start { s =>
  println("Attached to JVM: " + s.uniqueId)
}
```

As with the launching debugger, when you call `start`, you provide a function
that takes a `ScalaVirtualMachine` as an argument. This function is treated as
a callback that is invoked when the attaching debugger connects to the target
JVM process.

### Stopping

To stop the attaching debugger, you can call the `stop` function:

```scala
attachingDebugger.stop()
```

This disconnects from the target JVM and stops the debugger. The target JVM
will continue to run and, if suspended, should resume normal execution.

### Cookbook

See the [cookbook][cookbook-attaching] for a full example.

## Listening Debugger

The listening debugger is less frequently used as it offers the opposite mode
of connecting. While the launching and attaching debuggers serve to perform the
connection themselves, the listening debugger merely waits for target JVMs to
connect to it.

```
# Connect to a listening debugger via port 5005
-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=5005
```

!!! note "Note:"
    The listening debugger is often capable of supporting more than one target
    JVM connecting at once. This means that you could have a single debugger
    process managing multiple target JVMs such as with a cluster setup.

### Instantiating

To create the listening debugger using the Scala debugger API, you instantiate
a new instance of the debugger using the companion object:

```scala
val listeningDebugger = ListeningDebugger(port = 5005)
```

The only required argument for the listening debugger is the port that the
debugger will use to listen for incoming connections from target JVMs. This
port should match the port you provid in the JDWP arguments to the target
JVMs when starting them. The debugger itself should be started before the
target JVMs as the target JVMs only try to connect once.

The listening debugger can also take additional arguments including the hostname
to bind to when listening for connections (defaulting to localhost) and the
number of worker threads to use when processing new JVM connections (defaulting
to a single thread).

### Starting

When you are ready to begin listening for target JVMs, call the `start` method.

```scala
listeningDebugger.start { s =>
  println("Received connection from JVM: " + s.uniqueId)
}
```

While the listening debugger's `start` method does take a callback function
like the other two debuggers, this function can be invoked more than once. For
each connection with a target JVM that the debugger receives, it will invoke
the callback and pass it the `ScalaVirtualMachine` instance representing the
new connection.

### Stopping

To stop the listening debugger, you can call the `stop` function:

```scala
listeningDebugger.stop()
```

This stops the debugger from listening for JVM connections. The target JVMs
that were connected will continue to run and, if suspended, should resume
normal execution.

### Cookbook

See the [cookbook][cookbook-listening] for a full example.

## Process Debugger

The process debugger is much less frequently used and is a convenience to
connect to processes that have exposed JDI sockets and whose PIDs are also
known.

It begins with the target JVM already running with the necessary JDWP arguments
to expose a TCP port that the debugger will use to connect behind the scenes: 

```
# Allow debuggers to attach via port 5005
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

### Instantiating

To create the process debugger using the Scala debugger API, you instantiate
a new instance of the debugger using the companion object:

```scala
val processDebugger = ProcessDebugger(port = 5005)
```

The only required argument for the process debugger is the pid of the process
that the debugger will treat as the target JVM. This process should still have
a port exposed in the JDWP arguments as the debugger will determine the
port and connect using it.

The process debugger can also take a timeout argument, which is the maximum
time to wait (in milliseconds) for a successful connection before timing out.

### Starting

When you are ready to connect to the target JVM, call the `start` method.

```scala
processDebugger.start { s =>
  println("Connected to JVM: " + s.uniqueId)
}
```

As with the other debuggers, when you call `start`, you provide a function
that takes a `ScalaVirtualMachine` as an argument. This function is treated as
a callback that is invoked when the process debugger connects to the target
JVM process.

### Stopping

To stop the process debugger, you can call the `stop` function:

```scala
processDebugger.stop()
```

This disconnects from the target JVM and stops the debugger. The target JVM
will continue to run and, if suspended, should resume normal execution.

### Cookbook

See the [cookbook][cookbook-process] for a full example.

[connectors]: http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/conninv.html
[cookbook-launching]: /cookbook/creating-a-launching-debugger/
[cookbook-attaching]: /cookbook/creating-an-attaching-debugger/
[cookbook-listening]: /cookbook/creating-a-listening-debugger/
[cookbook-process]: /cookbook/creating-a-process-debugger/

*[JDI]: Java Debugger Interface

