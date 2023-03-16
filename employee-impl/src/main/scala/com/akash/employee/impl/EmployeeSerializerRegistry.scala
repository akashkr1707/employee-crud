package com.akash.employee.impl

import com.akash.employee.api.EmployeeDetails
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object EmployeeSerializerRegistry extends JsonSerializerRegistry {
  override val serializers =  List (
        JsonSerializer[EmployeeDetails],
  )
}
