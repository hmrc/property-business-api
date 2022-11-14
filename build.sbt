/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import sbt.complete.DefaultParsers._
import uk.gov.hmrc.DefaultBuildSettings.{ addTestReportOption, defaultSettings }
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import scala.sys.process._
import play.sbt.PlayImport.PlayKeys._

val appName = "property-business-api"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    scalaVersion := "2.12.15",
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(defaultSettings(): _*)
  .configs(ItTest)
  .settings(
    inConfig(ItTest)(Defaults.itSettings ++ headerSettings(ItTest) ++ automateHeaderSettings(ItTest)),
    ItTest / fork := true,
    Test / test := ((Test / test) dependsOn oasMerge).value,
    ItTest / unmanagedSourceDirectories := Seq((ItTest / baseDirectory).value / "it"),
    ItTest / unmanagedClasspath += baseDirectory.value / "resources",
    Runtime / unmanagedClasspath += baseDirectory.value / "resources",
    ItTest / javaOptions += "-Dlogger.resource=logback-test.xml",
    ItTest / parallelExecution := false,
    addTestReportOption(ItTest, directory = "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 7798)

lazy val oasMerge = taskKey[Unit]("""Runs './run_oas_merge.sh' to merge OpenAPI spec files""")
oasMerge := {
  val exitCode = (s"./run_oas_merge.sh").!
  if (exitCode != 0) {
    throw new MessageOnlyException("OpenAPI merge failed, run using verbose: sbt oasMergeVerbose")
  }
}

lazy val oasMergeVerbose = taskKey[Unit]("""Runs './run_oas_merge.sh -v' to merge OpenAPI spec files in verbose""")
oasMergeVerbose := {
  val exitCode = (s"./run_oas_merge.sh -v").!
  if (exitCode != 0) {
    throw new MessageOnlyException("OpenAPI merge failed!")
  }
}

