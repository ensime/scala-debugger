---
weight: 2
---
# Using Extra Arguments for Requests

---

## Overview

With both profiles and the low-level API, you are able to provide extra
arguments in the form of `JDIArgument` or one of its derivatives:
`JDIRequestArgument` or `JDIEventArgument`. The extra arguments are used to
set additional configuration options on requests as well as provide additional
filtering and logic when receiving events.

Methods like `getOrCreateBreakpointRequest` accept a variable
number of extra arguments at the end of the method (defaulting to no extra
arguments).

```scala
val s: ScalaVirtualMachine = /* some virtual machine */

// Set a custom property accessible on the request object AND limit reporting
// to the first three occurrences
s.getOrCreateBreakpointRequest(
  "file.scala", 
  37, 
  CustomProperty("key", "value"), 
  MaxTriggerFilter(3)
)
```

![JDI Event Process Steps][jdi-event-process-steps]


## Request Arguments

`JDIRequestArgument` instances come in two flavors: `JDIRequestProperty` and
`JDIRequestFilter`.

Properties are used to configure information about a request such as its
suspension level.

Filters are used to narrow down what a request reports. In the case of a
breakpoint request, you might limit the thread where the breakpoint can
occur or the specific instance of a class that can trigger the event.

### Request Properties

| Case Class            | Description                                                                                         |
| ----------            | -----------                                                                                         |
| EnabledProperty       | Sets whether or not the request is enabled.                                                         |
| SuspendPolicyProperty | Sets the suspend policy of the request.                                                             |
| CustomProperty        | Adds a key/value pair to the request that can be referenced when receiving events for that request. |
| UniqueIdProperty      | Adds a unique id to the request via a custom property.                                              |

### Request Filters

| Case Class           | Description                                                                                                      |
| ----------           | -----------                                                                                                      |
| ClassExclusionFilter | Limits reporting of events to only classes not specified by this filter.                                         |
| ClassInclusionFilter | Limits reporting of events to only classes specified by this filter.                                             |
| ClassReferenceFilter | Similar to `ClassInclusionFilter`, but takes a JDI `ReferenceType` instead of a string name.                     |
| CountFilter          | Limits reporting to only occur after N-1 occurrences of the event. Furthermore, the event is only reported once. |
| InstanceFilter       | Limits reporting of events to a specific instance of a class.                                                    |
| SourceNameFilter     | Limits reporting of events to classes whose source name matches the specified pattern.                           |
| ThreadFilter         | Limits reporting of events to the specific thread.                                                               |

## Event Arguments

`JDIEventArgument` instances come in two flavors: `JDIEventDataRequest` and
`JDIEventFilter`.

Data requests are used to collect information from an event and its
associated request and include it in callbacks and pipelines that expect
event data.

Filters are used to narrow down which events trigger callbacks and get fed to
pipelines. Common use cases include filtering based on a provided unique id
or custom property to a request (as the associated request is available in an
event).

There are also a couple of event arguments that directly extend
`JDIEventArgument` and are used for special purposes.

### Event Data Request

| Case Class                | Description                                                             |
| ----------                | -----------                                                             |
| CustomPropertyDataRequest | Retrieves a custom property from the request associated with the event. |

### Event Filter

| Case Class             | Description                                                                                                         |
| ----------             | -----------                                                                                                         |
| MaxTriggerFilter       | Limits triggering of event callbacks and pipelines to the first N events.                                           |
| MinTriggerFilter       | Limits triggering of event callbacks and pipelines to all but the first N events.                                   |
| MethodNameFilter       | Limits triggering of event callbacks and pipelines if they are locatable to only those whose method name matches.   |
| CustomPropertyFilter   | Limits triggering of event callbacks and pipelines to events whose requests contain a matching custom property.     |
| UniqueIdPropertyFilter | Limits triggering of event callbacks and pipelines to events whose requests contain a matching unique id.           |
| WildcardPatternFilter  | Limits triggering of event callbacks and pipelines to events whose method or class name matches the given wildcard. |

| Case Class | Description                                                                          |
| ---------- | -----------                                                                          |
| AndFilter  | Combines the truthiness of two other filters such that BOTH must allow the event.    |
| OrFilter   | Combines the truthiness of two other filters such that EITHER must allow the event.  |
| NotFilter  | Flips a filter such that an event is allowed only if the filter disallows the event. |

### Miscellaneous Event Arguments

| Case Class | Description                                                                         |
| ---------- | -----------                                                                         |
| NoResume   | If provided, does not resume the JVM after the event is processed.                  |
| YesResume  | If provided, does resume the JVM after the event is processed. This is the default. |

[jdi-event-process-steps]: /img/api/advanced-topics/jdi-event-process-steps.png

*[JDI]: Java Debugger Interface

