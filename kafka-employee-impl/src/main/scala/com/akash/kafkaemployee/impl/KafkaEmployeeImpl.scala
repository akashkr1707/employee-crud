package com.akash.kafkaemployee.impl

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import akka.{Done, NotUsed}
import com.akash.kafkaemployee.api.KafkaEmployeeService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}


case class StartConsumer(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession)

class KafkaEmployeeImpl(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession) extends  KafkaEmployeeService {
  implicit val actorSystem: ActorSystem = ActorSystem("employee-system")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  val kafkaConsumerActor: ActorRef = actorSystem.actorOf(Props[KafkaConsumer](), "kafka-producer")


  override def kafkaConsumer: ServiceCall[NotUsed, Done] =  ServiceCall { _ =>
    kafkaConsumerActor ! StartConsumer(persistentEntityRegistry, session)
        Future(Done)
  }
}
