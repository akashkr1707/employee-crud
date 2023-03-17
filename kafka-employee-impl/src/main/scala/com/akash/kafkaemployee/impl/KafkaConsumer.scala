package com.akash.kafkaemployee.impl

import akka.actor.{Actor, ActorSystem}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.{Done, NotUsed}
import com.akash.employee.api.EmployeeDetails
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class KafkaConsumer extends Actor {
 implicit val system: ActorSystem = ActorSystem("consumer-sys")
 implicit val mat: Materializer = ActorMaterializer()
 implicit val ec: ExecutionContextExecutor = system.dispatcher


 override def receive: Receive = {
  case StartConsumer(persistentEntityRegistry, session) => start(persistentEntityRegistry, session)
 }

 private def start(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession): Unit = {

  val config = ConfigFactory.load()

  val dbProfile = config.getString("config")

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile](dbProfile)

  implicit val postgresRepository: PostgresRepo = new PostgresRepo(databaseConfig)
  implicit val cassendraRepository: CassendraRepo = new CassendraRepo(persistentEntityRegistry, session)


  val consumerConfig = config.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)

  val kafkaSource: Source[ConsumerMessage.CommittableMessage[String, String],
    Consumer.Control] =
   Consumer
     .committableSource(consumerSettings, Subscriptions.topics("add-employee"))

  runnableaableGraphForEmployee(kafkaSource).runWith(Sink.ignore).onComplete {
   case Success(_) => println("Done"); system.terminate()
   case Failure(err) => println(err.toString); system.terminate()
  }
 }
  private def runnableaableGraphForEmployee(kafkaSource: Source[ConsumerMessage.CommittableMessage[String, String],
    Consumer.Control])(implicit postgresRepository: PostgresRepo,cassendraRepo: CassendraRepo ) = {

   val messageeDecodeFlow: Flow[CommittableMessage[String, String], String, NotUsed] = Flow[CommittableMessage[String, String]].map {
    message =>
     message.record.value
   }

   val parseEmployeeData: Flow[String, EmployeeDetails, NotUsed] = Flow[String].map {
    data =>
     Json.parse(data).as[EmployeeDetails]
   }

   def addEmployeeToDB[T](content: String)(implicit DBOps: DBOps[T]): Either[Throwable, Future[Int]] = {
    DBOps.addEmployee(content)
   }

   val processEmployeeData: Flow[EmployeeDetails, Either[Throwable, Future[Int]], NotUsed] = Flow[EmployeeDetails].map {
    case employee if employee.dbType == "C" => addEmployeeToDB[CassandraType](employee.content)
    case employee if employee.dbType == "P" => addEmployeeToDB[PostgresType](employee.content)
    case _ => Left(throw new Exception("Invalid DB type"))
   }

   val sink = kafkaSource
     .via(messageeDecodeFlow)
     .via(parseEmployeeData)
     .via(processEmployeeData)
   sink

  }
}


