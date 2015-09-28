Scala Debugger
==============

[![Build Status](https://travis-ci.org/chipsenkbeil/scala-debugger.svg?branch=master)](https://travis-ci.org/rcsenkbeil/scala-debugger)
[![Scaladoc 2.10](https://img.shields.io/badge/Scaladoc-2.10-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.senkbeil/scala-debugger-api_2.10)
[![Scaladoc 2.11](https://img.shields.io/badge/Scaladoc-2.11-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.senkbeil/scala-debugger-api_2.11)
[![Join the chat at https://gitter.im/rcsenkbeil/scala-debugger](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/rcsenkbeil/scala-debugger?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A simple debugger library for Scala.

Installing with SBT
-------------------

Hosted on Maven Central and can be installed via the following:

    libraryDependencies += "org.senkbeil" %% "scala-debugger-api" % "1.0.0"
    
Features
--------

Currently, the Scala debugger library supports low-level breakpoints and step functionality. Furthermore, it provides the low-level API to capture and process JDI (Java Debugger Interface) events from the JVM.

#### Breakpoints ####
```scala
val vm: ScalaVirtualMachine = /* Wrapper around standard Java JDI virtual machine */
val sourceName = "some-file.scala"
val sourceLineNumber = 109

// Set a breakpoint for line 109 of some-file.scala
vm.breakpointManager.setLineBreakpoint(sourceName, sourceLineNumber)

// Capture all breakpoint events, resuming upon finishing the callback
vm.eventManager.addResumingEventHandler(BreakpointEventType, e => {
  val breakpointEvent = e.asInstanceOf[BreakpointEvent]
  val location = breakpointEvent.location()
  val fileName = location.sourcePath()
  val lineNumber = location.lineNumber()

  println(s"Reached breakpoint: $fileName:$lineNumber")
  if (fileName == sourceName && lineNumber == sourceLineNumber) {
    println("Hit desired breakpoint!")
  }
})
```

#### Steps ####
```scala
val vm: ScalaVirtualMachine = /* Wrapper around standard Java JDI virtual machine */
val sourceName = "some-file.scala"
val sourceLineNumber = 109

// Set a breakpoint for line 109 of some-file.scala (will step past this)
vm.breakpointManager.setLineBreakpoint(sourceName, sourceLineNumber)

// Capture all breakpoint events, resuming upon finishing the callback
vm.eventManager.addResumingEventHandler(BreakpointEventType, e => {
  val breakpointEvent = e.asInstanceOf[BreakpointEvent]
  val location = breakpointEvent.location()
  val fileName = location.sourcePath()
  val lineNumber = location.lineNumber()

  if (fileName == sourceName && lineNumber == sourceLineNumber) {
    println("Hit desired breakpoint!")
    vm.stepManager.stepOver(breakpointEvent.thread())
  }
})

// Capture all step events, resuming upon finishing the callback
vm.eventManager.addResumingEventHandler(StepEventType, e => {
  val stepEvent = e.asInstanceOf[StepEvent]
  val className = stepEvent.location().declaringType().name()
  val lineNumber = stepEvent.location().lineNumber()

  logger.debug(s"Stepped onto $className:$lineNumber")
})
```

#### JDI Implicit Wrappers ####
```scala
// Import implicit wrappers for standard JDI types like StackFrameReference and ThreadReference
import org.senkbeil.debugger.wrappers._

val vm: ScalaVirtualMachine = /* Wrapper around standard Java JDI virtual machine */
val sourceName = "some-file.scala"
val sourceLineNumber = 109

// Set a breakpoint for line 109 of some-file.scala
vm.breakpointManager.setLineBreakpoint(sourceName, sourceLineNumber)

// Capture all breakpoint events, resuming upon finishing the callback
vm.eventManager.addResumingEventHandler(BreakpointEventType, e => {
  val breakpointEvent = e.asInstanceOf[BreakpointEvent]
  
  // Get the current stack frame where we hit the breakpoint
  val threadReference = breakpointEvent.thread()
  val currentFrame = threadReference.frame(0)

  // Print out the list of local variables at the breakpoint
  // NOTE: Retrieves the immediate value of each variable (non-recursive), so primitives are immediately available
  println(currentFrame.localVisibleVariableMap())
  
  // Print out the list of local fields (contained by "this") at the breakpoint
  // NOTE: Retrieves the immediate value of each variable (non-recursive), so primitives are immediately available
  println(currentFrame.thisVisibleFieldMap())
})
```

Potential Development Gotchas
-----------------------------

- When moving from Mac OS X's IntelliJ to the IntelliJ of Linux Mint, the
  tools.jar of OpenJDK/Oracle JDK was not picked up. I needed to manually open
  up the SDK being used and add the tools.jar of 
  `/usr/lib/jvm/java-7-openjdk-amd64/lib/` to the classpath. This allows me to
  use the Sun-based `com.sun.jdi` package for a Java debugger interface rather
  than C++.

- When using the launching debugger, I noticed that it was creating an address
  of senkbeil.org:RANDOMPORT. _senkbeil.org_ corresponded to my host name.
  Attempting to telnet into the address and port provided resulted in a failed
  connection. Switching my hostname to _locahost_ allowed the main process
  (and telnet) to connect and use the JVM process.

Other Development Notes
-----------------------

- After observing IntelliJ's Scala plugin and the Scala IDE, it appears that
  stepping into a frame is not as simple as one would like in Scala. The
  process appears to use a series of hard-coded class and methods names to
  filter out when stepping into the next frame. This is performed using a
  series of step into and out of operations until the next valid location is
  reached.

