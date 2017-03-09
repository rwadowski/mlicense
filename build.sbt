val slf4jVersion = "1.7.21"
val logBackVersion = "1.1.7"
val scalaLoggingVersion = "3.5.0"
val slickVersion = "3.1.1"
val seleniumVersion = "2.53.0"
val circeVersion = "0.6.1"
val akkaVersion = "2.4.16"
val akkaHttpVersion = "10.0.3"

val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
val loggingStack = Seq(slf4jApi, logBackClassic, scalaLogging)

val typesafeConfig = "com.typesafe" % "config" % "1.3.1"

val circeCore = "io.circe" %% "circe-core" % circeVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeJawn = "io.circe" %% "circe-jawn" % circeVersion
val circe = Seq(circeCore, circeGeneric, circeJawn)

val javaxMailSun = "com.sun.mail" % "javax.mail" % "1.5.5"

val slick = "com.typesafe.slick" %% "slick" % slickVersion
val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
val h2 = "com.h2database" % "h2" % "1.3.176" //watch out! 1.4.190 is beta
val postgres = "org.postgresql" % "postgresql" % "9.4.1208"
val flyway = "org.flywaydb" % "flyway-core" % "4.0"
val slickStack = Seq(slick, h2, postgres, slickHikari, flyway)

val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
val unitTestingStack = Seq(scalatest)

val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
val seleniumStack = Seq(seleniumJava, seleniumFirefox)

val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
val akkaHttpExperimental = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
val akkaHttpSession = "com.softwaremill.akka-http-session" %% "core" % "0.3.0"
val akkaStack = Seq(akkaHttpCore, akkaHttpExperimental, akkaHttpTestkit, akkaHttpSession)

val commonDependencies = unitTestingStack ++ loggingStack

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  libraryDependencies ++= commonDependencies
)

lazy val backend: Project = (project in file("backend"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= slickStack ++ akkaStack ++ circe ++ Seq(javaxMailSun, typesafeConfig),
      mainClass in (Compile, run) := Some("com.morgenrete.mlicense.Main")
    )

lazy val frontend: Project = (project in file("frontend"))
    .settings(commonSettings)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "MLicense",
    mainClass in (Compile, run) := (mainClass in (Compile, run) in backend).value
  )
  .dependsOn(backend, frontend)
  .aggregate(backend, frontend)

parallelExecution in Test := false