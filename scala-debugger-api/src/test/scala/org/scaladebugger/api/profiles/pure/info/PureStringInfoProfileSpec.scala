package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.TypeInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureStringInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockReferenceType = mock[ReferenceType]
  private val mockStringReference = mock[StringReference]
  private val pureStringInfoProfile = new PureStringInfoProfile(
    mockScalaVirtualMachine, mockStringReference
  )(
    _virtualMachine = mockVirtualMachine,
    _threadReference = mockThreadReference,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureStringInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockStringReference

        val actual = pureStringInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }
  }
}
