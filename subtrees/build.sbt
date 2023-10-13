import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
//import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

//lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged                 := true,
  //  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    scalaVersion                    := "2.13.8",
    scalacOptions ++= List("-Xfatal-warnings", "-Wconf:src=routes/.*:silent", "-feature", "-language:higherKinds")
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(version := "1")
  .settings(defaultSettings(): _*)
  .settings(
    resolvers += Resolver.jcenterRepo
  )

val appName = "mtd-common"

dependencyUpdatesFilter -= moduleFilter(organization = "com.typesafe.play")
dependencyUpdatesFilter -= moduleFilter(name = "scala-library")
dependencyUpdatesFilter -= moduleFilter(name = "flexmark-all")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-play")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-scalacheck")
dependencyUpdatesFilter -= moduleFilter(name = "bootstrap-backend-play-28")
dependencyUpdatesFailBuild := true
