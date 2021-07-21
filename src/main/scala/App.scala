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
import db.DirectorDao._
import db.MovieDao._
import db.Seed

object App extends IOApp {
  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int].emap { yearInt =>
      Try(Year.of(yearInt))
        .toEither
        .leftMap { err =>
          ParseFailure(err.getMessage, err.getMessage)
        }
    }

  object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[Int]("director")
  object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

  def movieRoutes[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val movieDecoder: EntityDecoder[F, Movie] = jsonOf[F, Movie]

    HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(maybeYear) =>
        val moviesByDirector = findMoviesByDirector(director)
        maybeYear match {
          case Some(validatedYear) => validatedYear.fold(
            _ => BadRequest("The Year Was Badly Formatted"),
            year => {
              val moviesByDirectorAndYear = moviesByDirector.filter(_.year == year.getValue)
              Ok(moviesByDirector.asJson) 
            }
          )
          case None => Ok(moviesByDirector.asJson)
        }
        
      case GET -> Root / "movies" / IntVar(movieId) =>
        findMovieById(movieId) match {
          case Some(movie) => Ok(movie.asJson)
          case None => NotFound(s"No Movie with ID $movieId found in the database!")
        }
      case req @ POST -> Root / "movies" =>
        for {
          movie <- req.as[Movie]
          _ = addMovie(movie)
          res <- Ok()
        } yield res
    }
  }

  def directorRoutes[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val directorDecoder: EntityDecoder[F, Director] = jsonOf[F, Director]

    HttpRoutes.of[F] {
      case GET -> Root / "directors" => Ok(findAllDirectors().asJson)
      case req @ POST -> Root / "directors" =>
        for {
          director <- req.as[Director]
          _ = addDirector(director)
          res <- Ok()
        } yield res
    }
  }

  def fileRoutes: HttpRoutes[IO] = {
    val dsl = Http4sDsl[IO]
    import dsl._

    val html = new File("/Users/dor/Workspace/scala-server/src/main/scala/static/index.html")

    HttpRoutes.of[IO] {
      case req @ GET -> Root => StaticFile.fromFile(html, Some(req))
        .getOrElseF(NotFound("File not found!"))
    }
  }

  def allRoutes[F[_]: Concurrent]: HttpRoutes[F] = {
    movieRoutes[F] <+> directorRoutes[F]
  }

  override def run(args: List[String]): IO[ExitCode] = {
    Server.createTcpServer().start()
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:mem:test", "sa", "")

    Seed()

    val corsConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = false,
      allowedMethods = Some(Set("Get", "POST", "PUT", "PATCH", "DELETE")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )

    val routes = Router(
      "/" -> fileRoutes,
      "/api" -> allRoutes[IO]
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(CORS(routes, corsConfig))
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
