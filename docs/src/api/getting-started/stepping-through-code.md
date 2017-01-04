# Stepping through Code

---

## Overview

The following will provide you with a brief overview of stepping into, over,
and out of lines of code in a target JVM.

## Requirements

In order to step through your code, you need to be in a suspended state in your
target JVM. This typically means that you have placed a breakpoint somewhere in
your target JVM, hit it, and want to begin stepping from that location.

In the explanation below, you will see examples using breakpoints as a means to
reach a state where you can begin stepping through your JVM.

When stepping into, over, or out of lines/frames in your target JVM, you need
to be able to provide a thread reference (a JDI structure) to indicate which
suspended thread will step forward.

## Performing a step

Performing a step is a very simple process. First, you need to make sure that
your target JVM is in a state where it can perform a step. This means that the
thread where the step will be performed needs to be suspended. The easiest way
to achieve this state in the location where you want to start is to set a
breakpoint and step as a reaction when a breakpoint occurs.

```scala
val s: ScalaVirtualMachine = /* some virtual machine */
val fileName: String = /* desired starting file location */
val lineNumber: Int = /* desired starting line location */

s.onUnsafeBreakpoint(fileName, lineNumber).foreach(be => {
  val path = be.location().sourcePath()
  val line = be.location().lineNumber()

  println(s"Reached breakpoint for $path:$line")

  /* Step code would go here */
})
```

Once you are ready to perform a step, you need to decide what kind of step you
would like to perform. The Scala debugger API supports six kinds of steps:

1. Step into a line of code: `stepIntoLine`
2. Step over a line of code: `stepOverLine`
3. Step out of a line of code: `stepOutLine`
4. Step into a frame: `stepIntoMin`
5. Step over a frame: `stepOverMin`
6. Step out of a frame: `stepOutMin`

The more common step types are associated with lines of code, rather than
stack frames. All of these methods return a future that is completed when the
step operation has completed. Each of these methods also requires you to
provide a [`ThreadReference`][thread-reference] where the step will occur. You
can acquire the reference to the thread where a breakpoint occurred from the
breakpoint event as seen in the example below.

```scala
val be: BreakpointEvent = /* Breakpoint event from above code example */

// Step methods return a future that occurs when the step finishes
import scala.concurrent.ExecutionContext.Implicits.global
s.stepOverLine(be.thread()).foreach(se => {
  val path = se.location().sourcePath()
  val line = se.location().lineNumber()

  println(s"Stepped to $path:$line")
})
```

## Cookbook

See the [cookbook][cookbook] for a working example.

[thread-reference]: http://docs.oracle.com/javase/7/docs/jdk/api/jpda/jdi/com/sun/jdi/ThreadReference.html
[cookbook]: /cookbook/stepping-over-a-line-of-code/

*[JDI]: Java Debugger Interface

