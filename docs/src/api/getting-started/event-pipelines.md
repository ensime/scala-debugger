---
weight: 3
---
# Event Pipelines

---

## Overview

A pipeline, as its name implies, is used to build a series of operations
(chain a collection of functions) to be evaluated whenever data is processed
through the pipeline. With regard to the Scala debugger API, this occurs
whenever an event associated with the pipeline occurs.

Event pipelines are a key component of the high-level API available in the
Scala debugger API. Methods like `onUnsafeBreakpoint` and
`onUnsafeAccessWatchpoint` return pipelines directly while `onBreakpoint` and
`onAccessWatchpoint` return a `Try` whose success is a pipeline. See the
other sections and the Scaladoc for more information on individual methods.

## Example of Pipeline

A pipeline of `BreakpointEvent` objects will typically be
triggered whenever a breakpoint on the target JVM is hit. When that occurs,
all operations applied to the pipeline will be evaluated in sequential order.

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// This creates a new pipeline for a breakpoint
val pipeline: IdentityPipeline[BreakpointEvent] = s.onUnsafeBreakpoint("some/scala/file.scala", 37)

// Triggers whenever a breakpoint is hit
pipeline.foreach(_ => println("A breakpoint occurred!"))

// Triggers explicitly when line 37 of any file is hit
pipeline.map(_.location().lineNumber()).filter(_ == 37).foreach(_ => println("Line 37 was hit!"))
```

In the example above, a pipeline was created for a specific breakpoint. From
there, two children pipelines were created: one that is triggered whenever a
breakpoint occurs and one that is only _fully_ evaluated when a breakpoint
occurs on line 37.

## What can I do with a pipeline?

A pipeline has the following operations available:

| Method          | Description                                                                       |
| --------        | -----------                                                                       |
| map             | Transforms each element in the pipeline to something else.                        |
| flatMap         | Transforms each element in the pipeline to something else and flattens it.        |
| filter          | Filters the pipeline to only continue evaluating elements that yield true.        |
| filterNot       | Filters the pipeline to only continue evaluating elements that yield false.       |
| foreach         | Performs some action on each element in the pipeline.                             |
| transform       | Transforms the operation of a pipeline to another operation.                      |
| unionInput      | Combines the input of two pipelines into one new pipeline.                        |
| unionOutput     | Combines the output of two pipelines into one new pipeline.                       |
| noop            | Does nothing to the pipeline elements. Just passes them down the chain.           |
| metadata        | Combines the current pipeline data with its metadata.                             |
| failed          | Triggered when the pipeline fails to process at this specific stage.              |
| withMetadata    | Adds additional metadata to be associated with the new pipeline.                  |
| close           | Closes the pipeline so no new elements get processed by it.                       |
| toFuture        | Converts the pipeline to a future, evaluated on the next element and then closed. |
| process         | Processes the provided data through the pipeline.                                 |
| currentMetadata | Returns the metadata at this stage in the pipeline.                               |

## Warning on closing pipelines

When closing pipelines for events, the underlying event handlers that
funnel events through the pipelines are removed; however, the request
to send event data from the target JVM to the debugger still exists until
__ALL__ pipelines associated with the request are closed!

You can avoid this problem by providing close with the `CloseRemoveAll` flag.
This will remove the request and all underlying event handlers. This means
that any other pipeline created for the request will also stop receiving
events.

```scala
val p: IdentityPipeline[BreakpointEvent] = /* some breakpoint pipeline */

// Remove the request and ALL underlying event handlers
import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
p.close(data = CloseRemoveAll)
```

