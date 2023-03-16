package com.akash.kafkaemployee.impl.event

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class EmployeeEntity extends PersistentEntity {

  override type Command = EmployeeCommand[_]
  override type Event = EmployeeEvent
  override type State = NotUsed

  override def initialState = NotUsed.getInstance()

  override def behavior =
    Actions()
      .onCommand[AddEmployee, Done] {
        case (AddEmployee(employee), ctx, _) =>
          val event: EmployeeEvent = AddedEmployee(employee)
          ctx.thenPersist(event) { res =>
            ctx.reply(Done)
          }
      }.onEvent {
      case (_, state) =>
        state
    }
}
