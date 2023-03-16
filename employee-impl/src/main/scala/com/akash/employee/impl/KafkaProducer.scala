package com.akash.employee.impl

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.akash.employee.api.EmployeeDetails
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object KafkaProducer {

  def start(details: EmployeeDetails) = {

    implicit val system: ActorSystem = ActorSystem("producer-sys")
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val config: Config = ConfigFactory.load()
    val producerConfig: Config = config.getConfig("akka.kafka.producer")
    val producerSettings: ProducerSettings[String, String] = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

    val produce: Future[Done] =
      Source.single(details)
        .map(value => new ProducerRecord[String, String]("add-employee", Json.toJson(value).toString()))
        .runWith(Producer.plainSink(producerSettings))

    produce onComplete {
      case Success(_) => println("Done"); system.terminate()
      case Failure(err) => println(err.toString); system.terminate()
    }
  }
}
