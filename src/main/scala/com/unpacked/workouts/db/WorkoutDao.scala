package com.unpacked.workouts.db

import scalikejdbc._
import java.util.UUID

object WorkoutDao {
  implicit val session: AutoSession = AutoSession

  case class Workout(id: Option[String], name: String, goal: String)

  def addWorkout(workout: Workout): Unit =
    sql"insert into workout (NAME, GOAL) values (${workout.name}, ${workout.goal})"
      .update()
      .apply()

  def fetchAllWorkouts(): List[Workout] =
    sql"select * from workout"
      .map(res => Workout(res.stringOpt("ID"), res.string("NAME"), res.string("GOAL")))
      .list()
      .apply()

  def fetchWorkoutById(workoutId: UUID): Option[Workout] =
    sql"select * from workout where ID = $workoutId"
      .map(res => Workout(res.stringOpt("ID"), res.string("NAME"), res.string("GOAL")))
      .single()
      .apply()

  def deleteWorkout(workoutId: UUID): Unit =
    sql"delete from workout where ID = $workoutId"
      .update()
      .apply()

  def updateWorkout(workoutId: UUID, workout: Workout): Unit =
    sql"update workout set NAME = ${workout.name}, GOAL = ${workout.goal} where ID = $workoutId"
      .update()
      .apply()
}