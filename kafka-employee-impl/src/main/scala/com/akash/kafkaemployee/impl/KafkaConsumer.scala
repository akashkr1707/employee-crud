package com.akash.kafkaemployee.impl

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Sink, Source}
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

object  KafkaConsumer{
 def start(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession): Unit = {
  implicit val system: ActorSystem = ActorSystem("consumer-sys")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load()

  val dbProfile = config.getString("config")

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile](dbProfile)

  implicit val postgresRepository: PostgresRepo = new PostgresRepo(databaseConfig)
  implicit val cassendraRepository: CassendraRepo = new CassendraRepo(persistentEntityRegistry,session)


  val consumerConfig = config.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)

  val kafkaSource : Source[ConsumerMessage.CommittableMessage[String, String],
    Consumer.Control]  =
   Consumer
    .committableSource(consumerSettings, Subscriptions.topics("add-employee"))

  val messgaeDecodeFlow: Flow[CommittableMessage[String, String], String, NotUsed] = Flow[CommittableMessage[String, String]].map {
   message =>
    message.record.value
  }

  val parseEmployeeData: Flow[String, EmployeeDetails, NotUsed] = Flow[String].map {
   data =>
    Json.parse(data).as[EmployeeDetails]
  }

  def addEmployeeToDB[T](content: String)(implicit DBOps: DBOps[T]): Future[Done] = {
   DBOps.addEmployee(content)
  }
  val processEmployeeData = Flow[EmployeeDetails].map {
   case emp if emp.dbType == "C" => println(s"Adding employee ${emp.content} in Cassandra Db")
    addEmployeeToDB[CassandraAction](emp.content)
   case emp if emp.dbType == "P" =>
    addEmployeeToDB[PostgresAction](emp.content)
   case _ => println("invalid Db type")
  }



 val sink = kafkaSource
   .via(messgaeDecodeFlow)
   .via(parseEmployeeData)
   .via(processEmployeeData)
   .runWith(Sink.foreach(println))

sink  onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}
}


