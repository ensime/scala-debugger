package test

import java.util.UUID

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.{EventManager, EventType}
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.events.EventInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.reflect.ClassTag
import scala.util.Success

class TestRequestHelper[E <: Event, EI <: EventInfo, RequestArgs, CounterKey](
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override val eventManager: EventManager,
  override val etInstance: EventType.Value
)(
  implicit val eClassTag: ClassTag[E],
  eiClassTag: ClassTag[EI]
) extends RequestHelper[E, EI, RequestArgs, CounterKey](
  scalaVirtualMachine = scalaVirtualMachine,
  eventManager = eventManager,
  etInstance = etInstance,
  _newRequestId = UUID.randomUUID().toString,
  _newRequest = (requestId, _, _) => Success(requestId),
  _hasRequest = _ => false,
  _newEventInfo = (_, _, _) => eiClassTag.runtimeClass.newInstance().asInstanceOf[EI],
  _removeRequestById = _ => {},
  _retrieveRequestInfo = _ => None
)
