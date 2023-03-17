package com.akash.kafkaemployee.impl

import com.akash.kafkaemployee.impl.event.{AddEmployee, EmployeeData, EmployeeEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.{Random, UUID}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
trait DBOps[T] {
  def addEmployee(content: String): Either[Throwable, Future[Int]]

}

trait DbType
case class PostgresType() extends  DbType
case class CassandraType() extends  DbType

  class PostgresRepo(val config: DatabaseConfig[JdbcProfile]) extends DBOps[PostgresType] with Db  with  SlickTables {
    val employeeQuery = TableQuery[EmployeeTable]
    override def addEmployee(content: String): Either[Throwable, Future[Int]] = {

      val emp = Employee(name = content)
      val insertQuery = employeeQuery += emp
      Try(db.run(insertQuery)) match {
        case Success(value) =>
          Right(value)
        case Failure(exception) => Left(exception)
      }
      }

    def getAllEmployee(): Either[Throwable, Future[Seq[Employee]]] = {
Try(db.run(employeeQuery.result)) match {
  case Success(value) =>
    Right(value)
  case Failure(exception) => Left(exception)
}
    }

  }

  class CassendraRepo(persistentEntityRegistry: PersistentEntityRegistry, session: CassandraSession)
                      extends DBOps[CassandraType] {
    override def addEmployee(content: String): Either[Throwable, Future[Int]] = {
      val id = UUID.randomUUID()
      val res: Try[Future[AddEmployee#ReplyType]] = Try {
        persistentEntityRegistry
          .refFor[EmployeeEntity](s"$id").ask(AddEmployee(EmployeeData(id, content)))
      }


    res match {
      case Success(value) => Right(Future(1))
      case Failure(ex) => Left(ex)
    }
    }
  }



