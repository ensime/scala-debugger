---
weight: 1
---
# Commands

The following is documentation detailing the various commands available in _sdb_
and what each can do.

---

## Connecting to a JVM

### attach

Attaches to a JVM that is listening for a debugger on a TCP socket.

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| port          | Number | The port of the remote JVM to attach to. | N/A | Yes |
| hostname      | Text   | The name/IP of the host to connect to. | localhost | No |
| timeout       | Number | The amount of time to wait to connect in milliseconds, or -1 to wait forever. | -1 | No |

### attachp

Attaches to a JVM using its PID to look up the TCP port that the JVM is
listening on for connections.

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| pid           | Number | The pid of the remote JVM to attach to. | N/A | Yes |
| timeout       | Number | The amount of time to wait to connect in milliseconds, or -1 to wait forever. | -1 | No |

### listen

Listens on the specified port for remote JVMs to attach to the debugger.

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| port          | Number | The port to listen on for remote JVMs. | N/A | Yes |
| hostname      | Text   | The name/IP to bind to when listening. | localhost | No |

### launch

Launches a new JVM using the specified class and attaches to it.

| Argument Name | Type  | Description | Default | Required |
| ------------- | ----  | ----------- | ------- | -------- |
| class         | Text  | The fully-qualified class name to use as the entrypoint to the new JVM. | N/A | Yes |
| suspend       | Truth | Whether or not to suspend the JVM when started. | true | No |

### stop

Disconnects from all connected JVMs and stops the active debugger.

## Breakpoints

### bp

Creates a new breakpoint on the specified file's line.

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| file          | Text   | The full path to the file starting at the package. E.g. "org/scaladebugger/file.scala". | N/A | Yes |
| line          | Number | The line number within the file to set a breakpoint. | N/A | Yes |

### bpclear

Removes all breakpoints if no arguments given. Removes a single breakpoint if
the `file` and `line` arguments are provided.

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| file          | Text   | The full path to the file starting at the package. E.g. "org/scaladebugger/file.scala". | N/A | No |
| line          | Number | The line number within the file to remove a breakpoint. | N/A | No |

### bplist

Lists all breakpoint requests.

## Class/Method/Field Info

### classes

Lists all classes on the connected JVM.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which classes are returned. Supports wildcards. E.g. "org.scaladebugger.*". | "*" | No |
| filterNot     | Text | The filter to use to limit which classes are not returned. Supports wildcards. E.g. "java.*". | "*" | No |

### methods

Lists all methods for the specified class.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified class name whose methods to list. | N/A | Yes |
| filter        | Text | The filter to use to limit which methods are returned. Supports wildcards. E.g. "get*". | "*" | No |
| filterNot     | Text | The filter to use to limit which methods are not returned. Supports wildcards. E.g. "set*". | "*" | No |

### fields

Lists all fields for the specified class.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified class name whose fields to list. | N/A | Yes |
| filter        | Text | The filter to use to limit which fields are returned. Supports wildcards. E.g. "field*". | "*" | No |
| filterNot     | Text | The filter to use to limit which fields are not returned. Supports wildcards. E.g. "other*". | "*" | No |

## Exceptions

### catch

Creates a new request to suspend on exceptions. Handles exceptions that are
caught in a `try` as well as exceptions that are uncaught.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exceptions to handle. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

### catchc

Creates a new request to suspend on exceptions. Handles exceptions that are
caught in a `try`.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exceptions to handle. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

### catchu

Creates a new request to suspend on exceptions. Handles exceptions that are
uncaught.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exceptions to handle. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

### catchlist

Lists all exception requests.

### ignore

Removes a request targeting both caught and uncaught exceptions. If no
argument provided, removes the global exception handler for caught and
uncaught exceptions.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exception requests to remove. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

### ignorec

Removes a request targeting caught exceptions. If no
argument provided, removes the global exception handler for caught exceptions.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exception requests to remove. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

### ignoreu

Removes a request targeting uncaught exceptions. If no
argument provided, removes the global exception handler for uncaught exceptions.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| filter        | Text | The filter to use to limit which exception requests to remove. Supports wildcards. E.g. "my.exceptions.*". | "*" | No |

## Expressions

### examine

Prints information about the provided expression.

_Requires an active thread to be set._

_Currently, only variables and chained properties using dot notation are
supported._

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| expression    | Text | The expression to examine. E.g. "obj.field". | "*" | No |

### set

Sets the value for a variable to the specified new value.

_Requires an active thread to be set and suspended._

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| l             | Text | The variable to set. E.g. "obj.field". | N/A | Yes |
| r             | Any  | The new variable value. | N/A | Yes |

### locals

Lists the names and values of all variables and fields locally available in
the current thread's top stack frame.

_Requires an active thread to be set._

### dump

_Not yet implemented._

### eval

_Not yet implemented._

## Methods

### mentry

Creates a new request to suspend on entering a method.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose method to suspend on entry. | N/A | Yes |
| method        | Text | The name of the method to suspend on entry. | N/A | Yes |

### mentryclear

Clears a single method entry request for the specified method, or clears all
method entry requests for a specific class if no method specified.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose method entry request to remove. | N/A | Yes |
| method        | Text | The name of the method whose method entry request to remove. | N/A | No |

### mentrylist

Lists all method entry requests.

### mexit

Creates a new request to suspend on exiting a method.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose method to suspend on exit. | N/A | Yes |
| method        | Text | The name of the method to suspend on exit. | N/A | Yes |

### mexitclear

Clears a single method exit request for the specified method, or clears all
method exit requests for a specific class if no method specified.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose method exit request to remove. | N/A | Yes |
| method        | Text | The name of the method whose method exit request to remove. | N/A | No |

### mexitlist

Lists all method exit requests.

## Sources

### list

Prints the source code around the current location of the active thread.

_Requires an active thread to be set._

| Argument Name | Type   | Description | Default | Required |
| ------------- | ----   | ----------- | ------- | -------- |
| size          | Number | The number of lines to display above and below the current position. | 4 | No |

### sourcepath

Adds a new source path to the list used for source code printing, or displays
the current source paths if no argument provided.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| sourcepath    | Text | The relative (to sdb) or absolute path to a source directory. | N/A | No |

### sourcepathclear

Removes all source paths being used for source lookup.

## Steps

### stepin

From the current position in the active thread, steps into the next location on
a different line. For function calls, this enters into the function. Otherwise,
the position is moved to the next line.

### stepinm

From the current position in the active thread, steps into the next available
location, including any newly-pushed frames.

### stepout

From the current position in the active thread, steps into the next location on
a different line. This will finish evaluating the current function where the
thread is positioned and land on the line where the function was called.

### stepoutm

From the current position in the active thread, steps into the next available
location, stepping out of the current frame.

### stepover

From the current position in the active thread, steps into the next location on
a different line. For function calls, this will evaluate the function and
continue to the next line.

### stepoverm

From the current position in the active thread, steps into the next available
location, stepping over any newly-pushed frames.

## Threads

### threads

Lists current threads in the remote JVM.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| threadGroup   | Text | The specific thread group whose threads to list. | N/A | No |

### thread

Sets the active thread whose name matches the specified name. If no thread is
specified, clears the active thread.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| thread        | Text | The name of the thread. | N/A | No |

### suspend

Suspends the specified thread, or suspends all threads in the JVM if no
thread is specified.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| thread        | Text | The name of the thread. | N/A | No |

### resume

Resumes the specified thread, or resumes all threads in the JVM if no
thread is specified.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| thread        | Text | The name of the thread. | N/A | No |

### where

Dumps the current stack of frames (the locations) for the position of the
specified thread, or active thread if no thread is specified.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| thread        | Text | The name of the thread. | N/A | No |

## Thread Groups

### threadgroups

Lists current thread groups in the remote JVM.

### threadgroup

Sets the active thread group whose name matches the specified name.
If no thread group is specified, clears the active thread group.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| threadGroup   | Text | The name of the thread group. | N/A | No |

## Watchpoints

### watch

Creates requests to watch a variable for access and modification attempts.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. | N/A | Yes |
| field         | Text | The name of the field to watch. | N/A | Yes |

### watcha

Creates requests to watch a variable for access attempts.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. | N/A | Yes |
| field         | Text | The name of the field to watch. | N/A | Yes |

### watchm

Creates requests to watch a variable for modification attempts.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. | N/A | Yes |
| field         | Text | The name of the field to watch. | N/A | Yes |

### watchlist

Lists all watchpoint requests.

### unwatch

Removes a request watching both access and modification of a field. If no class
or field is specified, all watchpoint requests for access and modification are
removed. If a class is specified without a field, all watchpoints for the
specific class for access and modification are removed.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. Can include wildcards if no field specified. | N/A | No |
| field         | Text | The name of the field to watch. | N/A | No |

### unwatcha

Removes a request watching both access of a field. If no class
or field is specified, all watchpoint requests for access are
removed. If a class is specified without a field, all watchpoints for the
specific class for access are removed.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. Can include wildcards if no field specified. | N/A | No |
| field         | Text | The name of the field to watch. | N/A | No |

### unwatchm

Removes a request watching both modification of a field. If no class
or field is specified, all watchpoint requests for modification are
removed. If a class is specified without a field, all watchpoints for the
specific class for modification are removed.

| Argument Name | Type | Description | Default | Required |
| ------------- | ---- | ----------- | ------- | -------- |
| class         | Text | The fully-qualified name of the class whose field to watch. Can include wildcards if no field specified. | N/A | No |
| field         | Text | The name of the field to watch. | N/A | No |
