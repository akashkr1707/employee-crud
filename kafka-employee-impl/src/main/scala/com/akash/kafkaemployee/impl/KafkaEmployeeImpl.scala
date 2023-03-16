package com.akash.kafkaemployee.impl

import akka.{Done, NotUsed}
import com.akash.kafkaemployee.api.KafkaEmployeeService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KafkaEmployeeImpl(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession) extends  KafkaEmployeeService {



  override def kafkaConsumer: ServiceCall[NotUsed, Done] =  ServiceCall { _ =>
    KafkaConsumer.start(persistentEntityRegistry, session)
        Future(Done)
  }
}
