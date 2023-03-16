package com.akash.employee.impl

import akka.Done
import akka.actor.{Actor, ActorSystem}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import com.akash.employee.api.EmployeeDetails
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class KafkaProducer(
                   ) extends Actor {


  implicit val actorSystem: ActorSystem = ActorSystem("employee-system")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  override def receive: Receive = {

    case Start(details: EmployeeDetails) =>
      val config: Config = ConfigFactory.load()
      val producerConfig: Config = config.getConfig("akka.kafka.producer")
      val producerSettings: ProducerSettings[String, String] = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

      val produce: Future[Done] =
        Source.single(details)
          .map(value => new ProducerRecord[String, String]("add-employee", Json.toJson(value).toString()))
          .runWith(Producer.plainSink(producerSettings))

      produce onComplete {
        case Success(_) => println("Done"); actorSystem.terminate()
        case Failure(err) => println(err.toString); actorSystem.terminate()
      }
  }
}
