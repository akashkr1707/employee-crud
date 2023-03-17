package com.akash.employee.api.model

import com.akash.employee.api.EmployeeDetails
import play.api.libs.json.{Json, OFormat}

case class AddEmployeeResponse(message: String)

object AddEmployeeResponse {
  implicit  val employeeDetailsFormat: OFormat[AddEmployeeResponse] = Json.format[AddEmployeeResponse]
}
