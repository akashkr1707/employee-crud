package com.akash.employee.impl

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import com.akash.employee.api.{EmployeeDetails, EmployeeService}
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}


case class Start(details: EmployeeDetails)
class EmployeeServiceImpl() extends EmployeeService {

  implicit val actorSystem: ActorSystem = ActorSystem("employee-system")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  override def addEmployee(id: String): ServiceCall[EmployeeDetails, Done] = ServiceCall { request =>

    val kafkaProducer: ActorRef = actorSystem.actorOf(Props[KafkaProducer](),"kafka-producer")
    kafkaProducer ! Start(request)
//    KafkaProducer.start(request)
//    KafkaConsumer.start(persistentEntityRegistry,session)
    Future(Done)

  }
}
