---
weight: 0
---
# Values

---

The Scala Debugger language has two types of values: primitives and functions.
Primitives serve as the core data representations in the language. Functions act
as containers for common instructions that can be repeated and influenced by
varying input.

## Primitives

| Type Name | Description | Code Representation |
| --------- | ----------- | ------------------- |
| Truth     | Represents true/false logic. | true, false |
| Number    | Represents any numeric value. | 123.456 |
| Text      | Represents a series of characters, typically thought of as a string. | "my text" |
| Undefined | Represents when input is not provided or not returned. | undefined |

## Functions

Comes in two flavors: native and interpreted. Native functions are written in
Scala and exposed to the Scala Debugger language such as `equal`. Interpreted
functions are written in the Scala Debugger language. For more information,
see the [section about functions][section_about_functions].

[section_about_functions]: /language/basic-concepts/functions
