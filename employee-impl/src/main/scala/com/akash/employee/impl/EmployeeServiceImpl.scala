package com.akash.employee.impl

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.akash.employee.api.{EmployeeDetails, EmployeeService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.implicits._
import com.akash.employee.api.model.AddEmployeeResponse
import com.akash.employee.impl.utils.Constants
import com.lightbend.lagom.scaladsl.api.transport.BadRequest

case class Start(details: EmployeeDetails)
class EmployeeServiceImpl() extends EmployeeService {

  implicit val actorSystem: ActorSystem = ActorSystem("employee-system")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout = Timeout(5.seconds)
  val kafkaProducer: ActorRef = actorSystem.actorOf(Props[KafkaProducer](),"kafka-producer")

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[EmployeeServiceImpl])
  override def addEmployee(id: String): ServiceCall[EmployeeDetails, AddEmployeeResponse] = ServiceCall { request =>

    validaRequest(request) match {
      case Valid(_) =>
        kafkaProducer ! Start(request)
        Future(AddEmployeeResponse(Constants.MESSAGE_SENT_SUCCSSFULLY))

      case Invalid(e) =>
        log.info(s"Error while sending employee records to producer: ${e.head}")
        throw BadRequest(e.head)
    }


  }

  def validaRequest(request: EmployeeDetails): ValidatedNel[String, EmployeeDetails] = {
    val validName = validateName(request.content)
    val validDbType = validateDbType(request.dbType)
    (validName, validDbType).mapN((_, _) => request)
  }

  private def validateName(name: String): ValidatedNel[String, String] =
    if (name.nonEmpty) name.validNel else Constants.INVALID_EMPLOYEE.invalidNel

  private def validateDbType(dbType: String): ValidatedNel[String, String] =
    if (dbType.contains("P") || dbType.contains("C")) dbType.validNel else Constants.INVALID_DBTYPE.invalidNel
}
