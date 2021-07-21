package db

import scalikejdbc._

object DirectorDao {
  implicit val session: AutoSession = AutoSession

  case class Director(firstName: String, lastName: String) {
    override def toString = s"$firstName $lastName"
  }

  def addDirector(director: Director): Unit = {
    sql"insert into director (FIRSTNAME, LASTNAME) values (${director.firstName}, ${director.lastName})"
      .update()
      .apply()
  }

  def findAllDirectors(): List[Director] = {
    sql"select FIRSTNAME, LASTNAME from director"
      .map(res => Director(res.string("FIRSTNAME"), res.string("LASTNAME")))
      .list()
      .apply()
  }
}
