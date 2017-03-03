---
weight: 4
---
# Blocks

---

Blocks represent both a means to group expressions together as well as provide
a form of scoping in the Scala Debugger language.

## Defining a Block

A block is represented using open and closed curly braces, `{` and `}`.

```
{
  "this is a block"
}
```

## Return Value of a Block

The last expression in a block is returned as its result.

```
x := {
  1 + 1
  "test string"
  true
  999
}

print(x)
```

## Block Scoping

Blocks provide a new scope whenever introduced. Variables declared inside this
scope are not available outside of it. Variables declared outside the scope
are available within it.

```
x := 1

{
  x := x + 1
  y := x + 2
}

print(x)
"cannot print y, unavailable at this point"
```

