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

package config

import shared.models.domain.TaxYear
import cats.data.Validated
import cats.implicits.catsSyntaxValidatedId
import com.typesafe.config.Config
import config.Deprecation.{Deprecated, NotDeprecated}
import play.api.{ConfigLoader, Configuration}
import shared.routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import javax.inject.{Inject, Singleton}

trait AppConfig {
  // MTD ID Lookup Config
  def mtdIdBaseUrl: String

  def desDownstreamConfig: DownstreamConfig
  def ifsDownstreamConfig: DownstreamConfig
  def tysIfsDownstreamConfig: DownstreamConfig
  def hipDownstreamConfig: BasicAuthDownstreamConfig

  // API Config
  def apiGatewayContext: String
  def confidenceLevelConfig: ConfidenceLevelConfig
  def apiStatus(version: Version): String
  def deprecationFor(version: Version): Validated[String, Deprecation]
  def apiDocumentationUrl: String
  def featureSwitches: Configuration
  def endpointsEnabled(version: String): Boolean

  def endpointsEnabled(version: Version): Boolean
  def safeEndpointsEnabled(version: String): Boolean

  /** Currently only for OAS documentation.
    */
  def apiVersionReleasedInProduction(version: String): Boolean

  /** Currently only for OAS documentation.
    */
  def endpointReleasedInProduction(version: String, name: String): Boolean

  def minimumTaxV2Foreign: TaxYear
  def minimumTaxV2Uk: TaxYear

  def minimumTaxYearHistoric: TaxYear
  def maximumTaxYearHistoric: TaxYear

  def endpointAllowsSupportingAgents(endpointName: String): Boolean
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, val configuration: Configuration) extends AppConfig {

  val appName: String = config.getString("appName")

  // MTD ID Lookup Config
  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  private def serviceKeyFor(serviceName: String) = s"microservice.services.$serviceName"

  protected def downstreamConfig(serviceName: String): DownstreamConfig = {
    val baseUrl = config.baseUrl(serviceName)

    val serviceKey = serviceKeyFor(serviceName)

    val env                = config.getString(s"$serviceKey.env")
    val token              = config.getString(s"$serviceKey.token")
    val environmentHeaders = configuration.getOptional[Seq[String]](s"$serviceKey.environmentHeaders")

    DownstreamConfig(baseUrl, env, token, environmentHeaders)
  }

  protected def basicAuthDownstreamConfig(serviceName: String): BasicAuthDownstreamConfig = {
    val baseUrl = config.baseUrl(serviceName)

    val serviceKey = serviceKeyFor(serviceName)

    val env                = config.getString(s"$serviceKey.env")
    val clientId           = config.getString(s"$serviceKey.clientId")
    val clientSecret       = config.getString(s"$serviceKey.clientSecret")
    val environmentHeaders = configuration.getOptional[Seq[String]](s"$serviceKey.environmentHeaders")

    BasicAuthDownstreamConfig(baseUrl, env, clientId, clientSecret, environmentHeaders)
  }

  def desDownstreamConfig: DownstreamConfig          = downstreamConfig("des")
  def ifsDownstreamConfig: DownstreamConfig          = downstreamConfig("ifs")
  def tysIfsDownstreamConfig: DownstreamConfig       = downstreamConfig("tys-ifs")
  def hipDownstreamConfig: BasicAuthDownstreamConfig = basicAuthDownstreamConfig("hip")

  // API Config
  val apiGatewayContext: String                    = config.getString("api.gateway.context")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")
  def apiStatus(version: Version): String          = config.getString(s"api.${version.name}.status")
  def featureSwitches: Configuration               = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)
  def endpointsEnabled(version: String): Boolean   = config.getBoolean(s"api.$version.endpoints.enabled")

  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.${version.name}.endpoints.enabled")

  def safeEndpointsEnabled(version: String): Boolean =
    configuration
      .getOptional[Boolean](s"api.$version.endpoints.enabled")
      .getOrElse(false)

  def apiVersionReleasedInProduction(version: String): Boolean = config.getBoolean(s"api.$version.endpoints.api-released-in-production")

  def endpointReleasedInProduction(version: String, name: String): Boolean = {
    val versionReleasedInProd = apiVersionReleasedInProduction(version)
    val path                  = s"api.$version.endpoints.released-in-production.$name"

    val conf = configuration.underlying
    if (versionReleasedInProd && conf.hasPath(path)) config.getBoolean(path) else versionReleasedInProd
  }

  val minimumTaxV2Foreign: TaxYear = TaxYear.starting(config.getInt("minimum-tax-year.version-2.foreign"))
  val minimumTaxV2Uk: TaxYear      = TaxYear.starting(config.getInt("minimum-tax-year.version-2.uk"))

  val minimumTaxYearHistoric: TaxYear = TaxYear.starting(config.getInt("minimum-tax-year.version-2.historic"))
  val maximumTaxYearHistoric: TaxYear = TaxYear.starting(config.getInt("maximum-tax-year.version-2.historic"))

  val apiDocumentationUrl: String =
    config.getConfString("api.documentation-url", defString = s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/$appName")

  private val DATE_FORMATTER = new DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
    .toFormatter()

  def deprecationFor(version: Version): Validated[String, Deprecation] = {
    val isApiDeprecated: Boolean = apiStatus(version) == "DEPRECATED"

    val deprecatedOn: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.deprecatedOn")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val sunsetDate: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.sunsetDate")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val isSunsetEnabled: Boolean =
      configuration.getOptional[Boolean](s"api.$version.sunsetEnabled").getOrElse(true)

    if (isApiDeprecated) {
      (deprecatedOn, sunsetDate, isSunsetEnabled) match {
        case (Some(dO), Some(sD), true) =>
          returnDeprecationMessage(version, dO, sD)
        case (Some(dO), None, true) => Deprecated(dO, Some(dO.plusMonths(6).plusDays(1))).valid
        case (Some(dO), _, false)   => Deprecated(dO, None).valid
        case _                      => s"deprecatedOn date is required for a deprecated version $version".invalid
      }

    } else {
      NotDeprecated.valid
    }
  }

  private def returnDeprecationMessage(version: Version, dO: LocalDateTime, sD: LocalDateTime) = {
    if (sD.isAfter(dO)) {
      Deprecated(dO, Some(sD)).valid
    } else {
      s"sunsetDate must be later than deprecatedOn date for a deprecated version $version".invalid
    }
  }

  def endpointAllowsSupportingAgents(endpointName: String): Boolean =
    supportingAgentEndpoints.getOrElse(endpointName, false)

  private val supportingAgentEndpoints: Map[String, Boolean] =
    configuration
      .getOptional[Map[String, Boolean]]("api.supporting-agent-endpoints")
      .getOrElse(Map.empty)

}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
