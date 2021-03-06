ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "de.schwetschke"

lazy val zioCrawler = (project in file("."))
  .settings(
    name := "zio-crawler",
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.30.1",
    libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.8.8",
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC18-2",
    libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC18-2",
    libraryDependencies += "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12",
    libraryDependencies += "com.softwaremill.sttp.client" %% "core" % "2.0.6",
    libraryDependencies += "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.0.6",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "1.0.0",
  )