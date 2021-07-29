package com.scalaserver

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import io.circe.syntax._
import org.h2.tools.Server
import scalikejdbc.ConnectionPool
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.dsl.impl._
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import org.http4s.dsl.Http4sDsl
import java.time.Year
import scala.util.Try
import java.io.File
import db.WorkoutDao._
import db.Seed
import routers.WorkoutRouter
import routers.FileRouter

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:mem:test;MODE=PostgreSQL", "sa", "")

    Seed()

    val corsConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = false,
      allowedMethods = Some(Set("Get", "POST", "PUT", "PATCH", "DELETE")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )

    val routes = Router(
      "/" -> FileRouter[IO],
      "/workouts" -> WorkoutRouter[IO]
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(CORS(routes, corsConfig))
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
