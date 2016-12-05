package test

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.Random

/**
 * Contains helper methods to facilitate smaller test code for JDI.
 */
trait JDIMockHelpers { self: MockFactory =>
  private val random = new Random()

  def createStackFrameStub(
    thisObject: Option[ObjectReference] = None,
    visibleVariablesWithValues: Option[Seq[(LocalVariable, Value)]] = None,
    visibleVariablesWithoutValues: Option[Seq[LocalVariable]] = None
  ) = {
    val stackFrameStub = stub[StackFrame]

    if (thisObject.nonEmpty) {
      (stackFrameStub.thisObject _).when().returns(thisObject.get)
    } else {
      (stackFrameStub.thisObject _).when().throws(new Throwable)
    }

    if (visibleVariablesWithValues.nonEmpty ||
      visibleVariablesWithoutValues.nonEmpty) {
      val variables = (
        visibleVariablesWithValues.getOrElse(Nil) ++
        visibleVariablesWithoutValues.getOrElse(Nil).map((_, null: Value))
      ).map(_._1)

      (stackFrameStub.visibleVariables _).when().returns(variables.asJava)

      visibleVariablesWithValues.foreach(_.foreach { case (lv, v) =>
        (stackFrameStub.getValue _).when(lv).returns(v)
      })

      visibleVariablesWithoutValues.foreach(_.foreach { case (lv) =>
        (stackFrameStub.getValue _).when(lv).throws(new Throwable)
      })
    } else {
      (stackFrameStub.visibleVariables _).when().throws(new Throwable)
    }

    stackFrameStub
  }

  def createLocalVariableStub(name: String, isArgument: Boolean = false) = {
    val localVariable = stub[LocalVariable]
    (localVariable.name _).when().returns(name)
    (localVariable.isArgument _).when().returns(isArgument)

    localVariable
  }

  def createPrimitiveValueStub[T <: AnyVal](value: T)
    (implicit classTag: ClassTag[T]): Value =
  {
    val klass = classTag.runtimeClass
    if (klass eq classOf[Boolean]) {
      val valueStub = stub[BooleanValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Boolean])
      valueStub
    } else if (klass eq classOf[Byte]) {
      val valueStub = stub[ByteValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Byte])
      valueStub
    } else if (klass eq classOf[Char]) {
      val valueStub = stub[CharValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Char])
      valueStub
    } else if (klass eq classOf[Double]) {
      val valueStub = stub[DoubleValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Double])
      valueStub
    } else if (klass eq classOf[Float]) {
      val valueStub = stub[FloatValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Float])
      valueStub
    } else if (klass eq classOf[Int]) {
      val valueStub = stub[IntegerValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Int])
      valueStub
    } else if (klass eq classOf[Long]) {
      val valueStub = stub[LongValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Long])
      valueStub
    } else if (klass eq classOf[Short]) {
      val valueStub = stub[ShortValue]
      (valueStub.value _).when().returns(value.asInstanceOf[Short])
      valueStub
    } else {
      throw new Throwable("Unable to stub primitive: " + klass.getName)
    }
  }

  def createFieldStub(name: String) = {
    val field = stub[Field]
    (field.name _).when().returns(name)
    field
  }

  def createObjectReferenceStub(
    fieldsAndValues: Option[Seq[(Field, Value)]] = None,
    fieldsWithNoValues: Option[Seq[Field]] = None
  ) = {
    val value = stub[ObjectReference]

    // Set the visible fields to return the provided fields
    (value.referenceType _).when().returns({
      val referenceType = stub[ReferenceType]

      if (fieldsAndValues.nonEmpty || fieldsWithNoValues.nonEmpty) {
        (referenceType.visibleFields _).when().returns(
          (fieldsAndValues.getOrElse(Nil).map(_._1) ++
            fieldsWithNoValues.getOrElse(Nil)).asJava
        )
      } else {
        (referenceType.visibleFields _).when().throws(new Throwable)
      }

      referenceType
    })

    // Set the retrieval of a field's value to the associated value
    fieldsAndValues.foreach(_.foreach { case (f, v) =>
      (value.getValue _).when(f).returns(v)
    })

    // Set the retrieval of extra fields as an exception
    fieldsWithNoValues.foreach(_.foreach { case f =>
      (value.getValue _).when(f).throws(new Throwable)
    })

    value
  }

  /**
   * Creates a stub for a reference type using random values for needed
   * properties.
   *
   * @param extension The extension to use for the end of the file name
   * @param totalLocations The total number of locations to generate for this
   *                       reference type (use 0 for none, and -1 to throw an
   *                       exception)
   *
   * @return The new, stubbed reference type
   */
  def createRandomReferenceTypeStub(
    extension: String = java.util.UUID.randomUUID().toString,
    totalLocations: Int = 10
  ): ReferenceType = {
    val stubReferenceType = stub[ReferenceType]

    val name = java.util.UUID.randomUUID().toString
    val sourcePaths = Seq(
      java.util.UUID.randomUUID().toString + "." + extension
    )

    var locations: Seq[Location] = Nil
    var throwExceptionForLocations = false
    if (totalLocations > 0) {
      locations = (1 to totalLocations).map(_ => createRandomLocationStub())
    } else if (totalLocations == 0) {
      locations = Nil
    } else {
      locations = Nil
      throwExceptionForLocations = true
    }

    createReferenceTypeStub(
      name = name,
      sourcePaths = sourcePaths,
      locations = locations,
      throwExceptionForLocations = throwExceptionForLocations
    )
  }

  def createReferenceTypeStub(
    name: String,
    sourcePaths: Seq[String],
    locations: Seq[Location],
    throwExceptionForLocations: Boolean = false
  ): ReferenceType = {
    val stubReferenceType = stub[ReferenceType]

    (stubReferenceType.name _).when().returns(name)
    (stubReferenceType.sourcePaths _).when(*).returns(sourcePaths.asJava)
    (stubReferenceType.allLineLocations: Function0[java.util.List[Location]])
      .when().returns(locations.asJava)

    stubReferenceType
  }

  def createRandomLocationStub(): Location = {
    createLocationStub(random.nextInt())
  }

  def createLocationStub(lineNumber: Int, throwException: Boolean = false): Location = {
    val stubLocation = stub[Location]

    if (!throwException)
      (stubLocation.lineNumber: Function0[Int]).when().returns(lineNumber)
    else
      (stubLocation.lineNumber: Function0[Int]).when().throws(new Throwable)

    stubLocation
  }
}
