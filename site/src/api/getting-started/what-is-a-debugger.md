---
weight: 0
---
# What is a debugger?

In this section, you will get a brief overview of what a debugger is and the
features that it typically offers. You will also learn how the Scala debugger
API is related to the Eclipse debugger (described below).

---

## Overview

In a nutshell, a debugger is a program written to aid in testing and debugging
another computer program. For those unfamiliar, debugging is the process of
finding and removing defects (bugs) in a computer program and can be
accomplished in a variety of ways including examining log files for oddities,
monitoring the memory of an application or system, profiling for performance
bottlenecks, utilizing test code to discover inconsistencies, and using
interactive debugging to examine the running state of your program.

Debuggers come in quite a few different packages and, most likely, you have
encountered at least one. Several more well-known debuggers include [GDB][gdb],
commonly used to interactively debug C and C++ programs from the command line,
and the [Eclipse Debug Project][eclipse], which is part of the Eclipse IDE and
available for a variety of languages although most-specifically targeted
towards Java. If you have done any web-based frontend development, you have
probably used your browser's debugger such as
[Google Chrome's DevTools][chrome] or [Mozilla Firefox's Debugger][firefox].

While debuggers themselves are an old concept, the latest debuggers are
continuing to demonstrate new, clever ideas to make the everyday programmer's
life a little easier. If interested in modern developments in the debugger
space, check out Elm's [time traveling debugger][elm] or Scala IDE's new
[async debugger][scala-ide].

## Features

A debugger can offer a wide range of features to aid the programmer in
discovering and fixing defects. The most common features found in modern
debuggers are the ability to step through a program, stop (break) a program to
examine its state, and track the values of variables in a program.

### Running a program step-by-step

This involves running a target program one "line" at a time, breaking at each
next "line" to observe the state of the program such as the values of variables
in the stack frame. While line-by-line stepping is one of the most common and
useful forms of stepping through your program, some debuggers offer other step
increments such as frame-by-frame.

Newer debuggers like Scala IDE's [async debugger][scala-ide] offer the ability
to step through your program even when the code execution path is not
straightforward (in this case when running distributed, message-driven programs
using the [Akka toolkit and runtime][akka]).

### Breaking a program to examine its state

This involves setting what is known as a breakpoint, a marker in a program
indicating that you want to stop execution temporarily, to be able to examine
the running state of a program. Often, breakpoints are placed on specific
source lines of a program indicating that the developer wishes to stop the
program when executing a specific line to examine the current state. Earlier
breakpoints would be placed on specific instructions (assembly).

Other forms of breakpoints can also exist including setting a conditional
breakpoint that triggers when reading/writing memory (watchpoints) or the
evaluation of an expression yields true.

### Watching variables in a program

When debugging a program, a common scenario is that a developer wants to
know when a variable's value is accessed or changed. This typically involves
the temporary suspension of the program allowing the developer to inspect the
current stack frame to understand what led to the access/modification of the
variable. Furthermore, more restrictions might be placed on the modification
of a variable such that a developer is only notified when a variable is changed
to a certain value, allowing more accurate understanding of specific problems
such as when a variable unexpectedly becomes _null_.

### Evaluating expressions

A useful feature in many debuggers is the ability to evaluate an expression
(snippet of code) in the target program while it is suspended due to a
breakpoint. This allows developers to quickly test functions and perform
modifications to the state of the program using the same language as the
program itself.

## More about Eclipse and how it relates to this project

The [Eclipse Debug Project][eclipse] offers two subprojects for debugging with
the Eclipse tooling: platform debug and JDT debug. Platform debug focuses on
defining language-independent debugging capabilities including defining
breakpoints (see features for more info), launching programs, and
notification of events from target programs. JDT debug is more specific as it
implements the Java debugging support to launch or attach to a JVM, evaluate
expressions in the context of a stack frame, and dynamically reload classes
into the target JVM.

The underlying API driving Eclipse's debugging framework for Java (and other
JVM-based languages) is the [Java Platform Debugger Architecture][jpda] (JPDA).
The JDPA is a collection of APIs to debug Java code and includes the Java Debug
Wire Protocol (JDWP) defining communication between the debugger and target
JVMs, the Java Virtual Machine Tools Interface (JVMTI) providing a native
interface (C++) to inspect state and control execution of programs running in
a JVM, and the [Java Debugger Interface][jdi] (JDI) that defines a high-level
API in Java to debug remote JVMs.

The Scala debugger API is built on top of the JDI, wrapping existing JDI
functionality in Scala-friendly wrappers and providing higher-level
abstractions to aid Scala developers in writing debugger tooling and utilize
debugger functionality in their own applications.

[gdb]: https://www.gnu.org/software/gdb/
[eclipse]: http://www.eclipse.org/eclipse/debug/
[chrome]: https://developers.google.com/web/tools/chrome-devtools/debug/?hl=en
[firefox]: https://developer.mozilla.org/en-US/docs/Tools/Debugger
[elm]: http://elm-lang.org/blog/time-travel-made-easy
[scala-ide]: http://scala-ide.org/docs/current-user-doc/features/async-debugger/index.html
[akka]: http://akka.io/
[jpda]: http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/index.html
[jdi]: http://docs.oracle.com/javase/7/docs/jdk/api/jpda/jdi/index.html

*[JDI]: Java Debugger Interface

