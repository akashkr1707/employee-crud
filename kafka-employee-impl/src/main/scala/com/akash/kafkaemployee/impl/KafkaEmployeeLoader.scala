package com.akash.kafkaemployee.impl

import com.akash.kafkaemployee.api.KafkaEmployeeService
import com.akash.kafkaemployee.impl.event.{EmployeeEntity, EmployeeEventReadSideProcessor}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

class KafkaEmployeeLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KafkaEmployeeApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KafkaEmployeeApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KafkaEmployeeService])
}

abstract class KafkaEmployeeApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[KafkaEmployeeService](wire[KafkaEmployeeImpl])

  lazy val kafkaEmployeeService: KafkaEmployeeImpl = wire[KafkaEmployeeImpl]

  kafkaEmployeeService.kafkaConsumer.invoke()
  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = KafkaEmployeeSerializerRegistry

  persistentEntityRegistry.register(wire[EmployeeEntity])

  readSide.register(wire[EmployeeEventReadSideProcessor])


  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  //  clusterSharding.init(
  //    Entity(HelloState.typeKey)(
  //      entityContext => HelloBehavior.create(entityContext)
  //    )
  //  )

}
