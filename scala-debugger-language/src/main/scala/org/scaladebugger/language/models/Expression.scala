package org.scaladebugger.language.models
import acyclic.file

trait Expression
case class SkipEval(expression: Expression) extends Expression

trait NamedExpression extends Expression { val name: String }
case class Identifier(
  name: String,
  documentation: Option[String] = None
) extends NamedExpression
case class ExpressionGroup(expressions: Seq[Expression]) extends Expression

trait BaseValue extends Expression {
  /** Converts the expression to its equivalent Scala value. */
  def toScalaValue: AnyRef
}
case class Array(elements: Seq[BaseValue]) extends BaseValue {
  override def toScalaValue: AnyRef = elements.map(_.toScalaValue)
}

sealed trait Primitive extends BaseValue { val value: Any }
case class Truth(value: Boolean) extends Primitive {
  override def toScalaValue: AnyRef = value: java.lang.Boolean
}
case class Number(value: Double) extends Primitive {
  override def toScalaValue: AnyRef = value: java.lang.Double
}
case class Text(value: String) extends Primitive {
  override def toScalaValue: AnyRef = value
}
case object Undefined extends Primitive {
  case object Value { override def toString: String = "undefined" }
  val value: AnyRef = Value
  override def toScalaValue: AnyRef = value//.toString
}

sealed trait Operator extends Expression
case class PlusPlus(left: Expression, right: Expression) extends Operator
case class Plus(left: Expression, right: Expression) extends Operator
case class Minus(left: Expression, right: Expression) extends Operator
case class Multiply(left: Expression, right: Expression) extends Operator
case class Divide(left: Expression, right: Expression) extends Operator
case class Modulus(left: Expression, right: Expression) extends Operator

sealed trait Condition extends Operator
case class Less(left: Expression, right: Expression) extends Condition
case class LessEqual(left: Expression, right: Expression) extends Condition
case class Greater(left: Expression, right: Expression) extends Condition
case class GreaterEqual(left: Expression, right: Expression) extends Condition
case class Equal(left: Expression, right: Expression) extends Condition
case class NotEqual(left: Expression, right: Expression) extends Condition
