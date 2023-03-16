package com.akash.employee.impl

import akka.Done
import com.akash.employee.api.{EmployeeDetails, EmployeeService}
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmployeeServiceImpl() extends EmployeeService {

  override def addEmployee(id: String): ServiceCall[EmployeeDetails, Done] = ServiceCall { request =>
    println(request)
    KafkaProducer.start(request)
//    KafkaConsumer.start(persistentEntityRegistry,session)
    Future(Done)

  }
}
