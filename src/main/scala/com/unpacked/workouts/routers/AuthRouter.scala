package com.unpacked.workouts.routers

import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import com.unpacked.workouts.db.UserDao._
import com.github.t3hnar.bcrypt._
import org.http4s.EntityDecoder
import io.circe.syntax._
import io.circe.JsonObject
import org.http4s.circe._
import io.circe.generic.auto._
import scala.util.Try
import io.circe.Json

object AuthRouter {
  def apply[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    implicit val userDecoder: EntityDecoder[F, User] = jsonOf[F, User]

    HttpRoutes.of[F] {
      case req @ POST -> Root / "signup" =>
        for {
          user <- req.as[User]
          salt = BCrypt.gensalt()
          password = (user.password + salt).bcrypt
          _ = addUser(user.email, password, salt)
          res <- Ok()
        } yield res
      case req @ POST -> Root / "login" => 
        for {
          user <- req.as[User]
          res <- fetchUserByEmail(user.email) match {
            case Some(storedUser) => {
              val isVerified = (user.password + storedUser.salt).isBcrypted(storedUser.password)
              val response = Map(
                "id" -> storedUser.id,
                "email" -> storedUser.email
              )
              if (isVerified) Ok(response.asJson) else Forbidden("Wrong Email or Password")
            }
            case None => NotFound("Wrong Email or Password")
          }
        } yield res
    }
  }
}
