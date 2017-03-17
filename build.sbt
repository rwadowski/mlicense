import sbt.complete.DefaultParsers.spaceDelimited
import NativePackagerHelper._
import com.typesafe.sbt.SbtNativePackager.packageArchetype

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

lazy val updateNpm = taskKey[Unit]("Update npm")
lazy val npmTask = inputKey[Unit]("Run npm with arguments")

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  libraryDependencies ++= commonDependencies,
  parallelExecution in Test := false,
  updateNpm := {
    println("Updating npm dependencies")
    haltOnCmdResultError(Process("npm install", baseDirectory.value / ".." / "frontend").!)
    println("Updating yarn dependencies")
    haltOnCmdResultError(Process("yarn install", baseDirectory.value / ".." / "frontend").!)
    println("Done")
  },
  npmTask := {
    val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
    updateNpm.value
    val localNpmCommand = "npm " + taskName
    def buildWebpack() = {
      Process(localNpmCommand, baseDirectory.value / ".." / "frontend").!
    }
    println("Building with Webpack : " + taskName)
    haltOnCmdResultError(buildWebpack())
  }
)

def haltOnCmdResultError(result: Int) {
  if(result != 0) {
    throw new Exception("Build failed.")
  }
}

lazy val backend: Project = (project in file("backend"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= slickStack ++ akkaStack ++ circe ++ Seq(javaxMailSun, typesafeConfig),
      unmanagedResourceDirectories in Compile := {
        (unmanagedResourceDirectories in Compile).value ++ List(baseDirectory.value.getParentFile / frontend.base.getName / "dist" )
      },
      (compile in Compile) := {
        npmTask.toTask(" run build.prod").value
        (compile in Compile).value
      }
    )

lazy val frontend: Project = (project in file("frontend"))
    .settings(commonSettings)
    .settings(test in Test := (test in Test).dependsOn(npmTask.toTask(" test")).value)

lazy val rootProject = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "MLicense",
    version := "0.0.1",
    packageSummary := "Simple license server",
    packageDescription := "License server for applications with multiple lines.",
    maintainer := "Robert Wadowski <robert.wadowski@morgenrete.com>",
    mainClass in Compile := Some("com.morgenrete.mlicense.Main"),
    mappings in Universal ++= {
      val resources = baseDirectory.value / "backend" / "src" / "main" / "resources"
      val conf = resources / "application.conf"
      val logback = resources / "logback.xml"
      Seq(conf -> "conf/application.conf", logback -> "conf/logback.xml")
    },
    mappings in Universal ++= directory( baseDirectory.value / "backend" / "src" / "main" / "resources" / "db" ),
    mappings in Universal ++= directory( baseDirectory.value / "backend" / "src" / "main" / "resources" / "templates" )
  )
  .dependsOn(backend, frontend)
  .aggregate(backend, frontend)
