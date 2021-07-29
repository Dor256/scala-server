package com.unpacked.workouts.routers

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.StaticFile
import java.io.File
import cats.effect._


object FileRouter {
  def apply[F[_]: Async]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    val html = new File("/Users/dor/Workspace/scala-server/src/main/scala/static/index.html")

    HttpRoutes.of[F] {
      case req @ GET -> Root => StaticFile.fromFile(html, Some(req))
        .getOrElseF(NotFound("File not found!"))
    }
  }
}