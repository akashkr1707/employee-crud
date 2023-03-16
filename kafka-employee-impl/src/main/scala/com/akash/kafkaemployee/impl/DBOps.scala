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

  def addEmployee(content: String): Either[Throwable, Future[Int]]

}

trait DbAction
case class PostgresAction() extends  DbAction
case class CassandraAction() extends  DbAction

  class PostgresRepo(val config: DatabaseConfig[JdbcProfile]) extends DBOps[PostgresAction] with Db  with  SlickTables {
    override def addEmployee(content: String): Either[Throwable, Future[Int]] = {
      val employeeQuery = TableQuery[EmployeeTable]
      val emp = Employee(name = content)
      val insertQuery = employeeQuery += emp
      Try(db.run(insertQuery)) match {
        case Success(value) =>
          Right(value)
        case Failure(exception) => Left(exception)
      }
      }

  }

  class CassendraRepo(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession)
                      extends DBOps[CassandraAction] {
    override def addEmployee(content: String): Either[Throwable, Future[Int]] = {
      val res: Try[Future[AddEmployee#ReplyType]] = Try {
        persistentEntityRegistry
          .refFor[EmployeeEntity]("abc").ask(AddEmployee(EmployeeData("ktp", content)))
      }


    res match {
      case Success(value) => Right(Future(1))
      case Failure(ex) => Left(ex)
    }
    }
  }



