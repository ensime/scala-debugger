---
weight: 5
---
# Functions

---

Functions represent a core construct in the Scala Debugger language. They can
be used to group commonly-used expressions together and repeat them while
varying input.

## Defining a Function

Functions are defined using the `func` keyword, followed by the parameter list
wrapped in open and closed parentheses - `(` and `)` - and a function body
using the same structure as [blocks][blocks].

```
func() {}
```

Unlike many languages, there is no way to declare a named function. Instead,
you create a function and assign it to a variable.

```
my_func := func() {}
```

A function will always return the last expression in its body (or `undefined`)
if the body is empty.

```
my_func := func() { 3 }
```

Finally, a function can have input parameters simply by adding names to the
parameter list.

```
my_func := func(a) { a }
```

## Invoking a Function

There are several ways to invoke a function in the Scala Debugger language.

### Traditional Form

The traditional way is to use the function's name followed by a parenthesis, the
series of arguments separated by commas, and a closing parenthesis.

```
my_func := func(a, b, c) { (a + b) * c }
result := my_func(1, 5, 10)
```

It is also possible to specify the arguments by name, which allows the arguments
to be provided in a different order.

```
my_func := func(a, b, c) { (a + b) * c }
result := my_func(c=10, b=5, a=1)
```

Furthermore, it is possible to mix named and unnamed arguments, where the
unnamed arguments will fill in the parameters in the matching locations.

```
my_func := func(a, b, c) { (a + b) * c }
result := my_func(c=10, 5, a=1)
```

### Command Form

An alternative to the traditional form of function invocation is to use the
command notation, where the name of the function is followed by the arguments
with only space separating each one.

```
my_func := func(a, b, c) { (a + b) * c }
result := my_func 1 5 10
```

The idea behind this form is to allow REPLs to have a more command-like nature
when using the language. As with the traditional form, arguments may have their
names specified and may also be mixed with the unnamed form.

```
my_func := func(a, b, c) { (a + b) * c }
result := my_func c=10 5 a=1
```

[blocks]: /language/basic-concepts/blocks
