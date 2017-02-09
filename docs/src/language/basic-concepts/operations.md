---
weight: 2
---
# Operations

---

The Scala Debugger language supports a variety of operations out of the box.
These vary from logical equality comparisons to arithmetic computations.

## Calling an operator

Operators may either be used by their code representation, `3 + 4`, or as a
function, `plus(3, 4)` or `plus 3 4`.

## Logical

| Name | Description | Function Name | Code Representation |
| ---- | ----------- | ------------- | ------------------- |
| Equal | Compares two values, returning true if they are equivalent. | equal | 10 == 10 |
| Not Equal | Compares two values, returning true if they are not equivalent. | notEqual | 10 != 3 |
| Less Than | Compares two values, returning true if the left is numerically less than the right. | lessThan | 10 < 3 |
| Less Than or Equal | Compares two values, returning true if the left is numerically less than or equal to the right. | lessThanEqual | 10 <= 3 |
| Greater Than | Compares two values, returning true if the left is numerically greater than the right. | greaterThan | 10 > 3 |
| Greater Than or Equal | Compares two values, returning true if the left is numerically greater than or equal to the right. | greaterThanEqual | 10 >= 3 |

## Arithmetic

| Name | Description | Function Name | Code Representation |
| ---- | ----------- | ------------- | ------------------- |
| Plus | Mathematically adds two numbers together. | plus | 10 + 3 |
| Minus | Mathematically subtracts the right number from the left. | minus | 10 - 3 |
| Multiply | Mathematically multiplies two numbers together. | multiply | 10 * 3 |
| Divide | Mathematically divides the right number from the left. | divide | 10 / 3 |
| Modulus | Mathematically divides the right number from the left, returning the remainder. | modulus | 10 % 3 |

## Text

| Name | Description | Function Name | Code Representation |
| ---- | ----------- | ------------- | ------------------- |
| Plus Plus | Concatenates two text values together. | plusPlus | "my" ++ "text" |
