package com.akash.employee.impl

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.akash.employee.api.{EmployeeDetails, EmployeeService}
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

case class Start(details: EmployeeDetails)
class EmployeeServiceImpl() extends EmployeeService {

  implicit val actorSystem: ActorSystem = ActorSystem("employee-system")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout = Timeout(5.seconds)
  val kafkaProducer: ActorRef = actorSystem.actorOf(Props[KafkaProducer](),"kafka-producer")
  override def addEmployee(id: String): ServiceCall[EmployeeDetails, Done] = ServiceCall { request =>
    kafkaProducer ! Start(request)
    Future(Done)

  }
}
