name := "scala-server"

version := "0.1"

scalaVersion := "2.13.6"

// idePackagePrefix := Some("com.scalaserver")

lazy val Http4sVersion = "1.0.0-M21"
lazy val CirceVersion = "0.14.0-M5"
lazy val H2DatabaseVersion = "1.4.200"
lazy val ScalikeJDBCVersion = "3.5.0"
lazy val LogBackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "com.h2database" % "h2" % H2DatabaseVersion,
  "org.scalikejdbc" %% "scalikejdbc" % ScalikeJDBCVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % ScalikeJDBCVersion,
  "ch.qos.logback" % "logback-classic" % LogBackVersion
)
