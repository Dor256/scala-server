package routers

import org.http4s._
import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import db.WorkoutDao._

object WorkoutRouter {
  def apply[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    implicit val workoutDecoder: EntityDecoder[F, Workout] = jsonOf[F, Workout]

    HttpRoutes.of[F] {
      case GET -> Root => Ok(fetchAllWorkouts.asJson)
      case GET -> Root / UUIDVar(workoutId) =>
        fetchWorkoutById(workoutId) match {
          case Some(workout) => Ok(workout.asJson)
          case None => NotFound(s"Could not find workout with id $workoutId in the database")
        }
      case req @ POST -> Root =>
        for {
          workout <- req.as[Workout]
          _ = addWorkout(workout)
          res <- Ok()
        } yield res
      case req @ DELETE -> Root / UUIDVar(workoutId) => Ok(deleteWorkout(workoutId))
      case req @ PUT -> Root / UUIDVar(workoutId) =>
        for {
          workout <- req.as[Workout]
          _ = updateWorkout(workoutId, workout)
          res <- fetchWorkoutById(workoutId) match {
            case Some(workout) => Ok(workout.asJson)
            case None => NotFound(s"Could not find workout with id $workoutId in the database")
          }
        } yield res
        
    }
  }
}
