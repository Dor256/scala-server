package db

import scalikejdbc._

object Seed {
  implicit val session: AutoSession = AutoSession

  def apply(): Unit =
    sql"create table if not exists director(ID INT NOT NULL AUTO_INCREMENT, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50), PRIMARY KEY (ID))"
      .execute()
      .apply()

    sql"create table if not exists movie(ID INT NOT NULL AUTO_INCREMENT, TITLE VARCHAR(50), YEAR INT, DIRECTOR INT, PRIMARY KEY (ID))"
      .execute()
      .apply()
}