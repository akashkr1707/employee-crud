package com.akash.employee.api

import akka.Done
import com.akash.employee.api.model.AddEmployeeResponse
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Json, OFormat}

case class EmployeeDetails(dbType: String, content: String)

object EmployeeDetails {
  implicit  val employeeDetailsFormat: OFormat[EmployeeDetails] = Json.format[EmployeeDetails]
}

trait EmployeeService  extends  Service {

  def addEmployee(id: String): ServiceCall[EmployeeDetails, AddEmployeeResponse]

  override def descriptor: Descriptor = {
    import Service._
    named("employee")
      .withCalls(
        pathCall("/api/addEmployee/:id", addEmployee _)
      )
      .withAutoAcl(true)
  }

}
