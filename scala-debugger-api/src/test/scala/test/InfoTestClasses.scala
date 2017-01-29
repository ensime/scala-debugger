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

  trait TestCreateInfoTrait extends CreateInfo {
    override def createRemotely(value: AnyVal): ValueInfo = throwException()
    override def createRemotely(value: String): ValueInfo = throwException()
  }

  trait TestMiscInfoTrait extends MiscInfo {
    override def availableLinesForFile(fileName: String): Option[Seq[Int]] = throwException()
    override def commandLineArguments: Seq[String] = throwException()
    override def sourceNameToPaths(sourceName: String): Seq[String] = throwException()
    override def mainClassName: String = throwException()
  }

  class TestThreadGroupInfo extends TestObjectInfo with ThreadGroupInfo {
    override def toJavaInfo: ThreadGroupInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def name: String = throwException()
    override def threadGroups: Seq[ThreadGroupInfo] = throwException()
    override def threads: Seq[ThreadInfo] = throwException()
    override def suspend(): Unit = throwException()
    override def resume(): Unit = throwException()
    override def parent: Option[ThreadGroupInfo] = throwException()
    override def toJdiInstance: ThreadGroupReference = throwException()
  }

  class TestThreadInfo extends TestObjectInfo with ThreadInfo {
    override def toJavaInfo: ThreadInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def status: ThreadStatusInfo = throwException()
    override def suspend(): Unit = throwException()
    override def resume(): Unit = throwException()
    override def indexedFields: Seq[FieldVariableInfo] = throwException()
    override def `type`: ReferenceTypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def frames: Seq[FrameInfo] = throwException()
    override def rawFrames(index: Int, length: Int): Seq[FrameInfo] = throwException()
    override def name: String = throwException()
    override def frame(index: Int): FrameInfo = throwException()
    override def totalFrames: Int = throwException()
    override def toJdiInstance: ThreadReference = throwException()
    override def threadGroup: ThreadGroupInfo = throwException()
  }

  class TestThreadStatusInfo extends ThreadStatusInfo {
    override def statusCode: Int = throwException()
    override def isMonitor: Boolean = throwException()
    override def isUnknown: Boolean = throwException()
    override def suspendCount: Int = throwException()
    override def isSuspended: Boolean = throwException()
    override def isWait: Boolean = throwException()
    override def isSleeping: Boolean = throwException()
    override def isAtBreakpoint: Boolean = throwException()
    override def isNotStarted: Boolean = throwException()
    override def isRunning: Boolean = throwException()
    override def isZombie: Boolean = throwException()
  }

  class TestLocationInfo extends LocationInfo {
    override def toJavaInfo: LocationInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def toJdiInstance: Location = throwException()
    override def sourcePath: String = throwException()
    override def lineNumber: Int = throwException()
    override def sourceName: String = throwException()
    override def codeIndex: Long = throwException()
    override def declaringType: ReferenceTypeInfo = throwException()
    override def method: MethodInfo = throwException()
  }

  class TestValueInfo extends ValueInfo {
    override def toJavaInfo: ValueInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def `type`: TypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def isObject: Boolean = throwException()
    override def isPrimitive: Boolean = throwException()
    override def toStringInfo: StringInfo = throwException()
    override def toObjectInfo: ObjectInfo = throwException()
    override def toPrimitiveInfo: PrimitiveInfo = throwException()
    override def toLocalValue: Any = throwException()
    override def toArrayInfo: ArrayInfo = throwException()
    override def isString: Boolean = throwException()
    override def isArray: Boolean = throwException()
    override def isVoid: Boolean = throwException()
    override def isNull: Boolean = throwException()
    override def isClassLoader: Boolean = throwException()
    override def toClassObjectInfo: ClassObjectInfo = throwException()
    override def isThreadGroup: Boolean = throwException()
    override def toClassLoaderInfo: ClassLoaderInfo = throwException()
    override def toThreadInfo: ThreadInfo = throwException()
    override def isThread: Boolean = throwException()
    override def isClassObject: Boolean = throwException()
    override def toThreadGroupInfo: ThreadGroupInfo = throwException()
    override def toJdiInstance: Value = throwException()
  }

  class TestVariableInfo extends VariableInfo with TestCreateInfoTrait {
    override def toJavaInfo: VariableInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def offsetIndex: Int = throwException()
    override def typeName: String = throwException()
    override def `type`: TypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def name: String = throwException()
    override def toValueInfo: ValueInfo = throwException()
    override def setValueFromInfo(valueInfo: ValueInfo): ValueInfo = throwException()
    override def isArgument: Boolean = throwException()
    override def isLocal: Boolean = throwException()
    override def isField: Boolean = throwException()
    override def toJdiInstance: Mirror = throwException()
  }

  class TestObjectInfo extends TestValueInfo with ObjectInfo {
    override def toJavaInfo: ObjectInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def indexedFieldOption(name: String): Option[FieldVariableInfo] = throwException()
    override def methodOption(name: String, parameterTypeNames: String*): Option[MethodInfo] = throwException()
    override def fieldOption(name: String): Option[FieldVariableInfo] = throwException()
    override def indexedFields: Seq[FieldVariableInfo] = throwException()
    override def `type`: ReferenceTypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def uniqueId: Long = throwException()
    override def invoke(thread: ThreadInfo, methodInfoProfile: MethodInfo, arguments: Seq[Any], jdiArguments: JDIArgument*): ValueInfo = throwException()
    override def fields: Seq[FieldVariableInfo] = throwException()
    override def methods: Seq[MethodInfo] = throwException()
    override def toJdiInstance: ObjectReference = throwException()
    override def referenceType: ReferenceTypeInfo = throwException()
  }

  class TestMethodInfo extends MethodInfo {
    override def toJavaInfo: MethodInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def parameterTypes: Seq[TypeInfo] = throwException()
    override def returnType: TypeInfo = throwException()
    override def declaringType: ReferenceTypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def name: String = throwException()
    override def returnTypeName: String = throwException()
    override def parameterTypeNames: Seq[String] = throwException()
    override def toJdiInstance: Method = throwException()
  }

  class TestGrabInfoProfile extends GrabInfoProfile {
    override def threadGroup(threadGroupReference: ThreadGroupReference): ThreadGroupInfo = throwException()
    override def threadGroups: Seq[ThreadGroupInfo] = throwException()
    override def `object`(objectReference: ObjectReference): ObjectInfo = throwException()
    override def thread(threadReference: ThreadReference): ThreadInfo = throwException()
    override def classes: Seq[ReferenceTypeInfo] = throwException()
    override def threads: Seq[ThreadInfo] = throwException()
    override def `class`(referenceType: ReferenceType): ReferenceTypeInfo = throwException()
    override def location(location: Location): LocationInfo = throwException()
    override def `type`(_type: Type): TypeInfo = throwException()
    override def field(referenceType: ReferenceType, field: Field): FieldVariableInfo = throwException()
    override def field(objectReference: ObjectReference, field: Field): FieldVariableInfo = throwException()
    override def localVariable(stackFrame: StackFrame, localVariable: LocalVariable): VariableInfo = throwException()
    override def stackFrame(stackFrame: StackFrame): FrameInfo = throwException()
    override def method(method: Method): MethodInfo = throwException()
    override def value(value: Value): ValueInfo = throwException()
  }

  class TestFrameInfo extends FrameInfo {
    override def toJavaInfo: FrameInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def thisObjectOption: Option[ObjectInfo] = throwException()
    override def variableOption(name: String): Option[VariableInfo] = throwException()
    override def indexedVariableOption(name: String): Option[VariableInfo] = throwException()
    override def indexedArgumentLocalVariables: Seq[IndexedVariableInfo] = throwException()
    override def indexedFieldVariables: Seq[FieldVariableInfo] = throwException()
    override def indexedAllVariables: Seq[VariableInfo] = throwException()
    override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfo] = throwException()
    override def indexedLocalVariables: Seq[IndexedVariableInfo] = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def index: Int = throwException()
    override def currentThread: ThreadInfo = throwException()
    override def location: LocationInfo = throwException()
    override def fieldVariables: Seq[FieldVariableInfo] = throwException()
    override def allVariables: Seq[VariableInfo] = throwException()
    override def localVariables: Seq[IndexedVariableInfo] = throwException()
    override def argumentValues: Seq[ValueInfo] = throwException()
    override def nonArgumentLocalVariables: Seq[IndexedVariableInfo] = throwException()
    override def argumentLocalVariables: Seq[IndexedVariableInfo] = throwException()
    override def toJdiInstance: StackFrame = throwException()
  }

  class TestStringInfo extends TestObjectInfo with StringInfo {
    override def toJavaInfo: StringInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def toJdiInstance: StringReference = throwException()
  }

  class TestArrayInfo extends TestObjectInfo with ArrayInfo with TestCreateInfoTrait {
    override def toJavaInfo: ArrayInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def indexedFields: Seq[FieldVariableInfo] = throwException()
    override def indexedField(name: String): FieldVariableInfo = throwException()
    override def `type`: ArrayTypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def length: Int = throwException()
    override def value(index: Int): ValueInfo = throwException()
    override def values(index: Int, length: Int): Seq[ValueInfo] = throwException()
    override def values: Seq[ValueInfo] = throwException()
    override def setValueFromInfo(index: Int, value: ValueInfo): ValueInfo = throwException()
    override def setValuesFromInfo(index: Int, values: Seq[ValueInfo], srcIndex: Int, length: Int): Seq[ValueInfo] = throwException()
    override def setValuesFromInfo(values: Seq[ValueInfo]): Seq[ValueInfo] = throwException()
    override def toJdiInstance: ArrayReference = throwException()
  }

  class TestPrimitiveInfo extends TestValueInfo with PrimitiveInfo {
    override def toJavaInfo: PrimitiveInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def `type`: PrimitiveTypeInfo = throwException()
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

  class TestTypeInfo extends TypeInfo {
    override def toJavaInfo: TypeInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def toPrimitiveType: PrimitiveTypeInfo = throwException()
    override def toJdiInstance: Type = throwException()
    override def name: String = throwException()
    override def signature: String = throwException()
    override def isArrayType: Boolean = throwException()
    override def isClassType: Boolean = throwException()
    override def isInterfaceType: Boolean = throwException()
    override def isReferenceType: Boolean = throwException()
    override def isPrimitiveType: Boolean = throwException()
    override def isNullType: Boolean = throwException()
    override def toArrayType: ArrayTypeInfo = throwException()
    override def toClassType: ClassTypeInfo = throwException()
    override def toInterfaceType: InterfaceTypeInfo = throwException()
    override def toReferenceType: ReferenceTypeInfo = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
  }

  class TestReferenceTypeInfo extends TestTypeInfo with ReferenceTypeInfo {
    override def toJavaInfo: ReferenceTypeInfo = throwException()
    override def isJavaInfo: Boolean = throwException()
    override def indexedFieldOption(name: String): Option[FieldVariableInfo] = throwException()
    override def fieldOption(name: String): Option[FieldVariableInfo] = throwException()
    override def classLoaderOption: Option[ClassLoaderInfo] = throwException()
    override def indexedVisibleFields: Seq[FieldVariableInfo] = throwException()
    override def scalaVirtualMachine: ScalaVirtualMachine = throwException()
    override def toJdiInstance: ReferenceType = throwException()
    override def isFinal: Boolean = throwException()
    override def isPrepared: Boolean = throwException()
    override def genericSignature: Option[String] = throwException()
    override def visibleFields: Seq[FieldVariableInfo] = throwException()
    override def name: String = throwException()
    override def instances(maxInstances: Long): Seq[ObjectInfo] = throwException()
    override def isInitialized: Boolean = throwException()
    override def allFields: Seq[FieldVariableInfo] = throwException()
    override def sourceNames: Seq[String] = throwException()
    override def sourcePaths: Seq[String] = throwException()
    override def isStatic: Boolean = throwException()
    override def isAbstract: Boolean = throwException()
    override def allMethods: Seq[MethodInfo] = throwException()
    override def isVerified: Boolean = throwException()
    override def sourceDebugExtension: String = throwException()
    override def minorVersion: Int = throwException()
    override def locationsOfLine(line: Int): Seq[LocationInfo] = throwException()
    override def methods(name: String): Seq[MethodInfo] = throwException()
    override def visibleMethods: Seq[MethodInfo] = throwException()
    override def allLineLocations: Seq[LocationInfo] = throwException()
    override def classObject: ClassObjectInfo = throwException()
    override def majorVersion: Int = throwException()
    override def nestedTypes: Seq[ReferenceTypeInfo] = throwException()
    override def tryAllMethods: Try[Seq[MethodInfo]] = throwException()
    override def tryVisibleMethods: Try[Seq[MethodInfo]] = throwException()
    override def tryMethods(name: String): Try[Seq[MethodInfo]] = throwException()
    override def tryInstances(maxInstances: Long): Try[Seq[ObjectInfo]] = throwException()
    override def allInstances: Seq[ObjectInfo] = throwException()
    override def tryAllInstances: Try[Seq[ObjectInfo]] = throwException()
    override def tryAllLineLocations: Try[Seq[LocationInfo]] = throwException()
    override def tryLocationsOfLine(line: Int): Try[Seq[LocationInfo]] = throwException()
    override def tryMajorVersion: Try[Int] = throwException()
    override def tryMinorVersion: Try[Int] = throwException()
    override def trySourceDebugExtension: Try[String] = throwException()
    override def trySourceNames: Try[Seq[String]] = throwException()
    override def trySourcePaths: Try[Seq[String]] = throwException()
  }
}
