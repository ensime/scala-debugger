package test

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Contains test implementations of info classes. All methods throw an error.
 */
object InfoTestClasses {
  class NotOverriddenException extends Exception

  /** Exception thrown by all methods. */
  val DefaultException = new NotOverriddenException
  private def throwException() = throw DefaultException

  trait TestCreateInfoProfileTrait extends CreateInfoProfile {
    override def createRemotely(value: AnyVal): ValueInfoProfile = throwException()
    override def createRemotely(value: String): ValueInfoProfile = throwException()
  }

  trait TestMiscInfoProfileTrait extends MiscInfoProfile {
    override def availableLinesForFile(fileName: String): Option[Seq[Int]] = throwException()
    override def commandLineArguments: Seq[String] = throwException()
    override def sourceNameToPaths(sourceName: String): Seq[String] = throwException()
    override def mainClassName: String = throwException()
  }

  class TestThreadInfoProfile extends TestObjectInfoProfile with ThreadInfoProfile {
    override def typeInfo: ReferenceTypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def frames: Seq[FrameInfoProfile] = throwException()
    override def rawFrames(index: Int, length: Int): Seq[FrameInfoProfile] = throwException()
    override def name: String = throwException()
    override def frame(index: Int): FrameInfoProfile = throwException()
    override def totalFrames: Int = throwException()
    override def toJdiInstance: ThreadReference = throwException()
  }

  class TestLocationInfoProfile extends LocationInfoProfile {
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def toJdiInstance: Location = throwException()
    override def sourcePath: String = throwException()
    override def lineNumber: Int = throwException()
    override def sourceName: String = throwException()
    override def codeIndex: Long = throwException()
    override def declaringType: ReferenceTypeInfoProfile = throwException()
    override def method: MethodInfoProfile = throwException()
  }

  class TestValueInfoProfile extends ValueInfoProfile {
    override def typeInfo: TypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def isObject: Boolean = throwException()
    override def isPrimitive: Boolean = throwException()
    override def toObject: ObjectInfoProfile = throwException()
    override def toPrimitive: PrimitiveInfoProfile = throwException()
    override def toLocalValue: Any = throwException()
    override def toArray: ArrayInfoProfile = throwException()
    override def isString: Boolean = throwException()
    override def isArray: Boolean = throwException()
    override def isVoid: Boolean = throwException()
    override def isNull: Boolean = throwException()
    override def toJdiInstance: Value = throwException()
  }

  class TestVariableInfoProfile extends VariableInfoProfile with TestCreateInfoProfileTrait {
    override def offsetIndex: Int = throwException()
    override def typeName: String = throwException()
    override def typeInfo: TypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def name: String = throwException()
    override def toValue: ValueInfoProfile = throwException()
    override def setValueFromInfo(valueInfo: ValueInfoProfile): ValueInfoProfile = throwException()
    override def isArgument: Boolean = throwException()
    override def isLocal: Boolean = throwException()
    override def isField: Boolean = throwException()
    override def toJdiInstance: Mirror = throwException()
  }

  class TestObjectInfoProfile extends TestValueInfoProfile with ObjectInfoProfile {
    override def typeInfo: ReferenceTypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def uniqueId: Long = throwException()
    override def invoke(methodInfoProfile: MethodInfoProfile, arguments: Seq[Any], jdiArguments: JDIArgument*): ValueInfoProfile = throwException()
    override def invoke(methodName: String, parameterTypeNames: Seq[String], arguments: Seq[Any], jdiArguments: JDIArgument*): ValueInfoProfile = throwException()
    override def method(name: String, parameterTypeNames: String*): MethodInfoProfile = throwException()
    override def fields: Seq[VariableInfoProfile] = throwException()
    override def field(name: String): VariableInfoProfile = throwException()
    override def methods: Seq[MethodInfoProfile] = throwException()
    override def toJdiInstance: ObjectReference = throwException()
    override def referenceType: ReferenceTypeInfoProfile = throwException()
  }

  class TestMethodInfoProfile extends MethodInfoProfile {
    override def parameterTypeInfo: Seq[TypeInfoProfile] = throwException()
    override def returnTypeInfo: TypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def name: String = throwException()
    override def returnTypeName: String = throwException()
    override def parameterTypeNames: Seq[String] = throwException()
    override def toJdiInstance: Method = throwException()
  }

  class TestGrabInfoProfile extends GrabInfoProfile {
    override def thread(threadReference: ThreadReference): ThreadInfoProfile = throwException()
    override def thread(threadId: Long): ThreadInfoProfile = throwException()
    override def classes: Seq[ReferenceTypeInfoProfile] = throwException()
  }

  class TestFrameInfoProfile extends FrameInfoProfile {
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def index: Int = throwException()
    override def thisObject: ObjectInfoProfile = throwException()
    override def currentThread: ThreadInfoProfile = throwException()
    override def location: LocationInfoProfile = throwException()
    override def fieldVariables: Seq[VariableInfoProfile] = throwException()
    override def variable(name: String): VariableInfoProfile = throwException()
    override def allVariables: Seq[VariableInfoProfile] = throwException()
    override def localVariables: Seq[IndexedVariableInfoProfile] = throwException()
    override def argumentValues: Seq[ValueInfoProfile] = throwException()
    override def nonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = throwException()
    override def argumentLocalVariables: Seq[IndexedVariableInfoProfile] = throwException()
    override def toJdiInstance: StackFrame = throwException()
  }

  class TestArrayInfoProfile extends TestObjectInfoProfile with ArrayInfoProfile with TestCreateInfoProfileTrait {
    override def typeInfo: ArrayTypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def length: Int = throwException()
    override def value(index: Int): ValueInfoProfile = throwException()
    override def values(index: Int, length: Int): Seq[ValueInfoProfile] = throwException()
    override def values: Seq[ValueInfoProfile] = throwException()
    override def setValueFromInfo(index: Int, value: ValueInfoProfile): ValueInfoProfile = throwException()
    override def setValuesFromInfo(index: Int, values: Seq[ValueInfoProfile], srcIndex: Int, length: Int): Seq[ValueInfoProfile] = throwException()
    override def setValuesFromInfo(values: Seq[ValueInfoProfile]): Seq[ValueInfoProfile] = throwException()
    override def toJdiInstance: ArrayReference = throwException()
  }

  class TestPrimitiveInfoProfile extends TestValueInfoProfile with PrimitiveInfoProfile {
    override def typeInfo: PrimitiveTypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def toLocalValue: AnyVal = throwException()
    override def isBoolean: Boolean = throwException()
    override def isFloat: Boolean = throwException()
    override def isDouble: Boolean = throwException()
    override def isInteger: Boolean = throwException()
    override def isLong: Boolean = throwException()
    override def isChar: Boolean = throwException()
    override def isByte: Boolean = throwException()
    override def isShort: Boolean = throwException()
    override def toJdiInstance: PrimitiveValue = throwException()
  }

  class TestTypeInfoProfile extends TypeInfoProfile {
    override def toPrimitiveType: PrimitiveTypeInfoProfile = throwException()
    override def toJdiInstance: Type = throwException()
    override def name: String = throwException()
    override def signature: String = throwException()
    override def isArrayType: Boolean = throwException()
    override def isClassType: Boolean = throwException()
    override def isInterfaceType: Boolean = throwException()
    override def isReferenceType: Boolean = throwException()
    override def isPrimitiveType: Boolean = throwException()
    override def isNullType: Boolean = throwException()
    override def toArrayType: ArrayTypeInfoProfile = throwException()
    override def toClassType: ClassTypeInfoProfile = throwException()
    override def toInterfaceType: InterfaceTypeInfoProfile = throwException()
    override def toReferenceType: ReferenceTypeInfoProfile = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
  }

  class TestReferenceTypeInfoProfile extends TestTypeInfoProfile with ReferenceTypeInfoProfile {
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def toJdiInstance: ReferenceType = throwException()
    override def isFinal: Boolean = throwException()
    override def isPrepared: Boolean = throwException()
    override def genericSignature: Option[String] = throwException()
    override def visibleFields: Seq[VariableInfoProfile] = throwException()
    override def name: String = throwException()
    override def instances(maxInstances: Long): Seq[ObjectInfoProfile] = throwException()
    override def isInitialized: Boolean = throwException()
    override def allFields: Seq[VariableInfoProfile] = throwException()
    override def sourceNames: Seq[String] = throwException()
    override def sourcePaths: Seq[String] = throwException()
    override def isStatic: Boolean = throwException()
    override def isAbstract: Boolean = throwException()
    override def allMethods: Seq[MethodInfoProfile] = throwException()
    override def field(name: String): VariableInfoProfile = throwException()
    override def classLoader: ClassLoaderInfoProfile = throwException()
    override def isVerified: Boolean = throwException()
    override def sourceDebugExtension: String = throwException()
    override def minorVersion: Int = throwException()
    override def locationsOfLine(line: Int): Seq[LocationInfoProfile] = throwException()
    override def methods(name: String): Seq[MethodInfoProfile] = throwException()
    override def visibleMethods: Seq[MethodInfoProfile] = throwException()
    override def allLineLocations: Seq[LocationInfoProfile] = throwException()
    override def classObject: ClassObjectInfoProfile = throwException()
    override def majorVersion: Int = throwException()
    override def nestedTypes: Seq[ReferenceTypeInfoProfile] = throwException()
    override def tryAllFields: Try[Seq[VariableInfoProfile]] = throwException()
    override def tryVisibleFields: Try[Seq[VariableInfoProfile]] = throwException()
    override def tryField(name: String): Try[VariableInfoProfile] = throwException()
    override def tryAllMethods: Try[Seq[MethodInfoProfile]] = throwException()
    override def tryVisibleMethods: Try[Seq[MethodInfoProfile]] = throwException()
    override def tryMethods(name: String): Try[Seq[MethodInfoProfile]] = throwException()
    override def tryInstances(maxInstances: Long): Try[Seq[ObjectInfoProfile]] = throwException()
    override def allInstances: Seq[ObjectInfoProfile] = throwException()
    override def tryAllInstances: Try[Seq[ObjectInfoProfile]] = throwException()
    override def tryAllLineLocations: Try[Seq[LocationInfoProfile]] = throwException()
    override def tryLocationsOfLine(line: Int): Try[Seq[LocationInfoProfile]] = throwException()
    override def tryMajorVersion: Try[Int] = throwException()
    override def tryMinorVersion: Try[Int] = throwException()
    override def trySourceDebugExtension: Try[String] = throwException()
    override def trySourceNames: Try[Seq[String]] = throwException()
    override def trySourcePaths: Try[Seq[String]] = throwException()
  }
}
