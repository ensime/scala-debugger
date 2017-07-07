---
weight: 1
---
# Variables

---

The Scala Debugger language is dynamically typed with all variables being
_mutable_. Variables are not locked to a specific type, which means that the
underlying value of a variable can change from a _number_ to _text_ to even
a _function_.

## Assignment

Variables are assigned using the `:=` operator with the variable name on the
left-hand side as seen below:

```
my_variable := 3.3
```

Variables are updated in the exact same manner.

```
my_variable := 3.3
my_variable := "some text"
```

Assignments return the resulting value of the assignment, which means you
can inline assignments within other expressions such as chaining assignments
together. The following assigns `one_var` and `two_var` to the value `true`.

```
one_var := two_var := true
```

