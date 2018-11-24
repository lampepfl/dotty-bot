inThisBuild(List(
  organization := "ch.epfl.lamp",
  scalaVersion := "2.12.6",
  version      := "0.1.0-SNAPSHOT"
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

    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.1.9",     // http server
      "com.lihaoyi" %% "requests" % "0.1.4", // http client

      "com.novocode" % "junit-interface" % "0.11" % Test
    ),

    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),

    fork in run := true,
    cancelable in Global := true,


    // specify main and ignore tests when assembling
    mainClass in assembly := Some("dotty.bot.Main"),
    test in assembly := {}
  )
