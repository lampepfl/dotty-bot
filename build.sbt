inThisBuild(List(
  organization := "ch.epfl.lamp",
  scalaVersion := "2.12.4"
))

lazy val `dotty-bot` = project
  .in(file("."))
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xfatal-warnings"
    ),

    libraryDependencies ++= {
      val circeVersion = "0.7.0"
      val http4sVersion = "0.15.3"
      Seq(
        "com.novocode" % "junit-interface" % "0.11" % Test,
        "ch.qos.logback" % "logback-classic" % "1.1.7",
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-blaze-server" % http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion
      )
    },

    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),

    // specify main and ignore tests when assembling
    mainClass in assembly := Some("dotty.bot.Main"),
    test in assembly := {}
  )
