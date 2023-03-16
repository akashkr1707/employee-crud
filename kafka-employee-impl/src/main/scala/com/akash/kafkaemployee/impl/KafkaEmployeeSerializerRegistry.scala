package com.akash.kafkaemployee.impl

import com.akash.employee.api.EmployeeDetails
import com.akash.kafkaemployee.impl.event.{AddEmployee, AddedEmployee}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object KafkaEmployeeSerializerRegistry extends JsonSerializerRegistry {
  override val serializers: Seq[JsonSerializer[_ >: EmployeeDetails with AddEmployee with AddedEmployee <: Product]] =  List (
    JsonSerializer[EmployeeDetails],
    JsonSerializer[AddEmployee],
    JsonSerializer[AddedEmployee]
  )
}
