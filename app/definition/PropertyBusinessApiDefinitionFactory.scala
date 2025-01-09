/*
 * Copyright 2023 HM Revenue & Customs
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

package definition

import cats.data.Validated.Invalid
import config.AppConfig
import shared.routing._
import utils.Logging

import javax.inject.{Inject, Singleton}

@Singleton
class PropertyBusinessApiDefinitionFactory @Inject() (appConfig: AppConfig) extends Logging {

  lazy val definition: Definition =
    Definition(
      api = APIDefinition(
        name = "Property Business (MTD)",
        description = "An API for providing property business data",
        context = appConfig.apiGatewayContext,
        categories = List("INCOME_TAX_MTD"),
        versions = List(
          APIVersion(
            version = Version4,
            status = buildAPIStatus(Version4),
            endpointsEnabled = appConfig.endpointsEnabled(Version4)
          ),
          APIVersion(
            version = Version5,
            status = buildAPIStatus(Version5),
            endpointsEnabled = appConfig.endpointsEnabled(Version5)
          )
        ),
        requiresTrust = None
      )
    )

  private[definition] def buildAPIStatus(version: Version): APIStatus = {
    checkDeprecationConfigFor(version)

    APIStatus.parser
      .lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
      }
  }

  private def checkDeprecationConfigFor(version: Version): Unit = appConfig.deprecationFor(version) match {
    case Invalid(error) => throw new Exception(error)
    case _              => ()
  }

}
