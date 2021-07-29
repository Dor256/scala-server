package com.unpacked.workouts.db

import scalikejdbc._

object Seed {
  implicit val session: AutoSession = AutoSession

  def apply(): Unit =
    sql"create table if not exists workout(ID UUID DEFAULT RANDOM_UUID(), NAME VARCHAR(50), GOAL VARCHAR(50), PRIMARY KEY (ID))"
      .execute()
      .apply()

    sql"create table if not exists user(ID UUID DEFAULT RANDOM_UUID(), EMAIL VARCHAR(50), PASSWORD VARCHAR(100), SALT VARCHAR(50), PRIMARY KEY (ID))"
      .execute()
      .apply()
}