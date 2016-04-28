package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class PureClassTypeInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureClassTypeInfoProfile") {
    it("should be able to retrieve information about the class type for variables and fields") {
      val testClass = "org.scaladebugger.test.info.ClassType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 68, NoResume)
        .foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val frame = s.withProfile(PureDebugProfile.Name)
            .thread(t.get).topFrame

          val classFromInterfaceFromBaseInterfaceType = frame.variable("classFromInterfaceFromBaseInterface").typeInfo.toClassType
          val baseClassType = frame.variable("baseClass").typeInfo.toClassType
          val classFromBaseClassType = frame.variable("classFromBaseClass").typeInfo.toClassType
          val classFromIndirectBaseClassType = frame.variable("classFromIndirectBaseClass").typeInfo.toClassType
          val classFromBaseInterfaceType = frame.variable("classFromBaseInterface").typeInfo.toClassType
          val classFromIndirectBaseInterfaceType = frame.variable("classFromIndirectBaseInterface").typeInfo.toClassType
          val notEnumerationClassType = frame.variable("notEnumerationClass").typeInfo.toClassType
          val enumerationClassType = frame.variable("enumerationClass").typeInfo.toClassType
          val javaEnumerationType = frame.variable("javaEnumeration").typeInfo.toClassType
          val classWithMethodsType = frame.variable("classWithMethods").typeInfo.toClassType
          val classWithFieldsType = frame.variable("classWithFields").typeInfo.toClassType
          val classWithConstructorType = frame.variable("classWithConstructor").typeInfo.toClassType
          val classFromBaseClassUsingBaseClassDefinitionType =
            frame.variable("classFromBaseClassUsingBaseClassDefinition").typeInfo.toClassType
          val classWithStaticMethodsType = s.classes.find(_.name.endsWith("ClassWithStaticMethods$")).get.toClassType
          val javaStaticMethodsType = frame.variable("javaStaticMethodsClass").typeInfo.toClassType
          val objectType = s.classes.find(_.name == "java.lang.Object").get.toClassType

          // Type name should return the definition
          classFromBaseClassUsingBaseClassDefinitionType.name should
            be("org.scaladebugger.test.info.ClassType$BaseClass")

          // Should be able to get only direct subclasses
          baseClassType.subclasses.map(_.name) should contain only
            ("org.scaladebugger.test.info.ClassType$ClassFromBaseClass")

          // Should be able to get directly-implemented interfaces
          classFromBaseInterfaceType.interfaces.map(_.name) should contain only
            ("org.scaladebugger.test.info.ClassType$BaseInterface")

          // Should be able to get directly and indirectly implemented interfaces
          classFromInterfaceFromBaseInterfaceType.allInterfaces.map(_.name) should contain only
            ("org.scaladebugger.test.info.ClassType$BaseInterface",
              "org.scaladebugger.test.info.ClassType$InterfaceFromBaseInterface")

          // Should be able to get the superclass of a class
          classFromBaseClassType.superclassOption.map(_.name).get should
            be("org.scaladebugger.test.info.ClassType$BaseClass")
          baseClassType.superclassOption.map(_.name).get should be("java.lang.Object")
          objectType.superclassOption.map(_.name) should be(None)

          // Should be able to determine whether a class is a Java enumeration
          notEnumerationClassType.isEnumeration should be(false)
          enumerationClassType.isEnumeration should be(false) // Scala enumeration is not Java
          javaEnumerationType.isEnumeration should be(true)

          // Should be able to find methods using their names and signatures
          classWithMethodsType.methodOption("method1", "()V").get.name should endWith("method1")
          classWithMethodsType.methodOption("method2", "(I)I").get.name should endWith("method2")
          classWithMethodsType.methodOption("method3", "(I)I").get.name should endWith("method3")
          classWithMethodsType.methodOption("method3", "(ILjava/lang/String;)Ljava/lang/String;").get.name should endWith("method3")
          classWithMethodsType.methodOption("invalid", "signature") should be(None)

          // Should be able to find object methods using their names and signatures
          classWithStaticMethodsType.methodOption("staticMethod1", "()V").get.name should endWith("staticMethod1")
          classWithStaticMethodsType.methodOption("staticMethod2", "(I)I").get.name should endWith("staticMethod2")
          classWithStaticMethodsType.methodOption("staticMethod3", "(I)I").get.name should endWith("staticMethod3")
          classWithStaticMethodsType.methodOption("staticMethod3", "(ILjava/lang/String;)Ljava/lang/String;").get.name should endWith("staticMethod3")
          classWithStaticMethodsType.methodOption("invalid", "signature") should be(None)

          // Should be able to find static methods using their names and signatures
          javaStaticMethodsType.methodOption("staticMethod1", "()V").get.name should endWith("staticMethod1")
          javaStaticMethodsType.methodOption("staticMethod2", "(I)I").get.name should endWith("staticMethod2")
          javaStaticMethodsType.methodOption("staticMethod3", "(I)I").get.name should endWith("staticMethod3")
          javaStaticMethodsType.methodOption("staticMethod3", "(ILjava/lang/String;)Ljava/lang/String;").get.name should endWith("staticMethod3")
          javaStaticMethodsType.methodOption("invalid", "signature") should be(None)

          // Should be able to find constructors
          classWithConstructorType.methodOption("<init>", "(ILjava/lang/String;)V").get.name should endWith("<init>")
        })
      }
    }

    it("should be able to invoke static methods") {
      val testClass = "org.scaladebugger.test.info.ClassType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 68, NoResume)
        .foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val thread = s.withProfile(PureDebugProfile.Name).thread(t.get)
          val frame = thread.topFrame

          val javaStaticMethodsType = frame.variable("javaStaticMethodsClass").typeInfo.toClassType

          // Should be able to find static methods using their names and signatures
          val method = javaStaticMethodsType.methodOption("staticMethod3", "(I)I").get
          val result = javaStaticMethodsType.invokeStaticMethod(
            thread,
            method,
            Seq(33)
          )

          result.toLocalValue should be (33)
        })
      }
    }

    it("should be able to create a new instance of the class") {
      val testClass = "org.scaladebugger.test.info.ClassType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 68, NoResume)
        .foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val thread = s.withProfile(PureDebugProfile.Name).thread(t.get)
          val frame = thread.topFrame

          val baseClassType = frame.variable("baseClass").typeInfo.toClassType
          val newBaseClass = baseClassType.newInstance(
            thread,
            "<init>",
            "()V",
            Nil
          )

          newBaseClass.isNull should be (false)
          newBaseClass.typeInfo.name should endWith ("BaseClass")
        })
      }
    }
  }
}
