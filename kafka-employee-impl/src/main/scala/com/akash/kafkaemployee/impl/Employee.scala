package com.akash.kafkaemployee.impl

import com.akash.kafkaemployee.impl.Db
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final case class Employee(id: Option[Long] = None,name: String)

trait SlickTables { this: Db =>


  class EmployeeTable(tag: Tag) extends Table[Employee](tag,"employee2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    override def * : ProvenShape[Employee] = (id.?, name).shaped <> (Employee.tupled, Employee.unapply)
  }
}
