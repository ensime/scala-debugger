---
weight: 3
---
# Conditions

---

Conditions are used to define branching behavior in the Scala Debugger
language.

## Defining a Condition

A basic condition consists of a single expression defining an
_if this, do that_ notion.

```
x := 3

if (x == 3) print("Success!")
```

As with any other expression, a condition returns a value.

```
x := 1
x := if (x == 1) 2
print(x)
```

If the condition is not met, `undefined` will be returned instead.

```
x := 1
x := if (x != 1) 2
print(x)
```

## Providing an Alternative Result

If an alternative result is needed when a condition is not true, the `else`
keyword can be used.

```
x := 999

if (x == 3) print("Should not get here!") else print("x was " ++ x)
```

## Chaining Together Conditions

Conditions can be chained together to form an _if this, then that; else if
this then that; else ..._ notion.

```
x := 999

if (x == 3) {
    print("x is 3")
} else if (x == 999) {
    print("x is 999")
} else {
    print("what is x?")
}
```
