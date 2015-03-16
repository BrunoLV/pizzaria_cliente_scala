name := "pizzaria_cliente_scala"

version := "1.0"

lazy val `pizzaria_cliente_scala` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(jdbc, anorm, cache, ws, "org.postgresql" % "postgresql" % "9.4-1200-jdbc41")


unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")