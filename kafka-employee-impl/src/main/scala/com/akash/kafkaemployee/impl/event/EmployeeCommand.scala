package com.akash.kafkaemployee.impl.event

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import play.api.libs.json.{Format, Json}

import java.util.UUID

sealed trait EmployeeCommand[R] extends ReplyType[R]

case class AddEmployee(employee: EmployeeData) extends EmployeeCommand[Done]

object AddEmployee {
  implicit val format: Format[AddEmployee] = Json.format[AddEmployee]
}

sealed trait EmployeeEvent extends AggregateEvent[EmployeeEvent] {
  override def aggregateTag: AggregateEventShards[EmployeeEvent] = EmployeeEvent.Tag
}

object EmployeeEvent {
  val NumShards = 3
  val Tag: AggregateEventShards[EmployeeEvent] = AggregateEventTag.sharded[EmployeeEvent](NumShards)
}

case class AddedEmployee(employee: EmployeeData) extends EmployeeEvent

object AddedEmployee {
  implicit val format: Format[AddedEmployee] = Json.format[AddedEmployee]

}
case class EmployeeData(id: UUID, name: String)

object EmployeeData {
  implicit val format: Format[EmployeeData] = Json.format[EmployeeData]
}
