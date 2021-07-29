package com.unpacked.workouts

import cats.effect._
import scalikejdbc.ConnectionPool
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.blaze.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import db.Seed
import routers.WorkoutRouter
import routers.FileRouter
import com.unpacked.workouts.routers.AuthRouter

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:mem:test;MODE=PostgreSQL", "sa", "")
  
    Seed()

    val corsConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = false,
      allowedMethods = Some(Set("GET", "POST", "PUT", "PATCH", "DELETE")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )
  
    val routes = Router(
      "/" -> FileRouter[IO],
      "/workouts" -> WorkoutRouter[IO],
      "/auth" -> AuthRouter[IO]
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(CORS(routes, corsConfig))
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
