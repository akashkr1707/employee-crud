package com.akash.kafkaemployee.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}


trait KafkaEmployeeService  extends  Service {

  def kafkaConsumer: ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("kafka-consumer")
      .withCalls(
        pathCall("/api/kafka-consumer", kafkaConsumer _)
      )
      .withAutoAcl(true)
  }

}
