package com.akash.kafkaemployee.impl.event

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}

import scala.concurrent.{ExecutionContext, Future}

class EmployeeEventReadSideProcessor(db: CassandraSession, readSide: CassandraReadSide
                                    )(implicit ec: ExecutionContext)
  extends ReadSideProcessor[EmployeeEvent] {

  private var insertEmployee: PreparedStatement = _

  override def buildHandler() = readSide.builder[EmployeeEvent]("EmployeeEventReadSideProcessor")
    .setGlobalPrepare(createTable)
    .setPrepare(_ => prepareStatements())
    .setEventHandler[AddedEmployee](ese => insertEmployee(ese.event.employee))
    .build()

  override def aggregateTags: Set[AggregateEventTag[EmployeeEvent]] = EmployeeEvent.Tag.allTags

  /**
   * Creates a table at the start up of the application.
   *
   * @return
   */
  private def createTable(): Future[Done] = {
    db.executeCreateTable(
      """CREATE TABLE IF NOT EXISTS employee.employeedata (
        |id text PRIMARY KEY, name text)""".stripMargin)
  }

  private def prepareStatements(): Future[Done] = {
    db.prepare("INSERT INTO employee.employeedata (id, name) VALUES (?, ?)")
      .map { ps =>
        insertEmployee = ps
        Done
      }
  }

  private def insertEmployee(employee: EmployeeData): Future[List[BoundStatement]] = {
    val bindInsertEmployee: BoundStatement = insertEmployee.bind()
    bindInsertEmployee.setString("id", employee.id)
    bindInsertEmployee.setString("name", employee.name)
    Future.successful(List(bindInsertEmployee))
  }
}
