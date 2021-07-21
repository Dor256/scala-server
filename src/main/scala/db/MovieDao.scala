package db

import scalikejdbc._

object MovieDao {
  implicit val session: AutoSession = AutoSession

  case class Movie(id: Int, title: String, year: Int)

  def findMovieById(movieId: Int): Option[Movie] =
    sql"select * from movie where ID = $movieId"
      .map(res => Movie(res.int("ID"), res.string("TITLE"), res.int("YEAR")))
      .single()
      .apply()

  def findMoviesByDirector(director: Int): List[Movie] =
    sql"select * from movie where DIRECTOR = $director"
      .map(res => Movie(res.int("ID"), res.string("TITLE"), res.int("YEAR")))
      .list()
      .apply()

  def addMovie(movie: Movie): Unit =
    sql"insert into movie (TITLE, YEAR) values (${movie.title}, ${movie.year})"
      .update()
      .apply()
}
