package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.profiles.traits.info.FieldVariableInfo

trait Scala210FieldTransformationRules {
  // Will retrieve fields of these objects
  private val expandNames = Seq("$outer")

  // Will skip these fields
  // TODO: Provide better means of skipping executionStart
  //       since this avoids it even if someone added it directly
  private val ignoreNames = Seq("MODULE$", "executionStart", "serialVersionUID")

  // Will skip fields whose names start with these prefixes
  private val ignoreNamePrefix = Seq("scala$")

  // Will skip fields whose origin starts with these strings
  private val ignoreOrigin = Seq("scala.")

  // TODO: Handle infinitely-recursive fields when expanding
  protected def transformField(field: FieldVariableInfo, isInStaticContext: Boolean = false): Seq[FieldVariableInfo] = {
    val jField = field.toJavaInfo
    val value = if (field.toJdiInstance.isStatic == isInStaticContext) Some(field.toValueInfo)
                else None

    // If the field is something we should ignore directly, do so
    if (ignoreNames.contains(jField.name)) Nil

    // If the field name starts with a prefix we should ignore, do so
    else if (ignoreNamePrefix.exists(jField.name.startsWith)) Nil

    // If the field's origin starts with an origin we don't want, ignore it
    else if (ignoreOrigin.exists(jField.declaringType.name.startsWith)) Nil

    // If the field is one that should be expanded, do so
    else if (value.exists(_.isObject) && expandNames.contains(jField.name))
      value.toList.flatMap(_.toObjectInfo.fields.flatMap(transformField(_)))

    // Otherwise, return the normal field
    else Seq(field)
  }
}
