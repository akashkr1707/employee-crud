package com.akash.kafkaemployee.impl

import akka.Done
import akka.Done.done
import com.akash.kafkaemployee.impl.event.{AddEmployee, EmployeeData, EmployeeEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait DBOps[T] {

  def addEmployee(content: String): Future[Done]

}

trait DbAction
case class PostgresAction() extends  DbAction
case class CassandraAction() extends  DbAction

  class PostgresRepo(val config: DatabaseConfig[JdbcProfile]) extends DBOps[PostgresAction] with Db  with  SlickTables {
    override def addEmployee(content: String): Future[Done] = {
      val employeeQuery = TableQuery[EmployeeTable]
      println("I nside postgres")
      val emp = Employee(name = content)
      val insertQuery = employeeQuery += emp
      Try(db.run(insertQuery)) match {
        case Success(value) =>
          Right(value)
        case Failure(exception) => Left(exception)
      }
      Future(Done)
      }

  }

  class CassendraRepo(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession)
                      extends DBOps[CassandraAction] {
    override def addEmployee(content: String): Future[Done] = {
      println("I nside Cassendra")

      persistentEntityRegistry
        .refFor[EmployeeEntity]("abc").ask(AddEmployee(EmployeeData("ktp", content))).map {
        res =>
          res
      }
    }
  }



