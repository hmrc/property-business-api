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

import cats.implicits.catsSyntaxValidatedId
import config.ConfidenceLevelConfig
import config.Deprecation.NotDeprecated
import definition.APIStatus.{ALPHA, BETA}
import mocks.{MockAppConfig, MockHttpClient}
import routing._
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "individuals/business/property"
    def checkBuildApiStatus(version: Version): APIStatus = apiDefinitionFactory.buildAPIStatus(version)
  }

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200
  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.deprecationFor(Version4).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.deprecationFor(Version5).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.apiStatus(Version3) returns "BETA"
        MockedAppConfig.apiStatus(Version4) returns "BETA"
        MockedAppConfig.apiStatus(Version5) returns "BETA"
        MockedAppConfig.endpointsEnabled(Version2).returns(true).anyNumberOfTimes()
        MockedAppConfig.endpointsEnabled(Version3).returns(true).anyNumberOfTimes()
        MockedAppConfig.endpointsEnabled(Version4).returns(true).anyNumberOfTimes()
        MockedAppConfig.endpointsEnabled(Version5).returns(true).anyNumberOfTimes()
        MockedAppConfig.confidenceLevelCheckEnabled
          .returns(ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = true, authValidationEnabled = true))
          .anyNumberOfTimes()

        private val readScope  = "read:self-assessment"
        private val writeScope = "write:self-assessment"

        apiDefinitionFactory.definition shouldBe
          Definition(
            scopes = List(
              Scope(
                key = readScope,
                name = "View your Self Assessment information",
                description = "Allow read access to self assessment data",
                confidenceLevel
              ),
              Scope(
                key = writeScope,
                name = "Change your Self Assessment information",
                description = "Allow write access to self assessment data",
                confidenceLevel
              )
            ),
            api = APIDefinition(
              name = "Property Business (MTD)",
              description = "An API for providing property business data",
              context = "individuals/business/property",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
                APIVersion(
                  version = Version2,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version3,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version4,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version5,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "confidenceLevel" when {
    List(
      (true, ConfidenceLevel.L250, ConfidenceLevel.L250),
      (true, ConfidenceLevel.L200, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L200, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, configCL, expectedDefinitionCL) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled and confidence-level = $configCL" should {
        s"return confidence level $expectedDefinitionCL" in new Test {
          MockedAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
            confidenceLevel = configCL,
            definitionEnabled = definitionEnabled,
            authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe expectedDefinitionCL
        }
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      List(
        (Version2, BETA),
        (Version3, BETA),
        (Version4, BETA),
        (Version5, BETA)
      ).foreach { case (version, status) =>
        s"return the correct $status for $version" in new Test {
          MockedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
          MockedAppConfig.apiStatus(version) returns status.toString
          apiDefinitionFactory.buildAPIStatus(version) shouldBe status
        }
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version2) returns "ALPHO"
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(version = Version2) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw exception" in new Test {
        MockedAppConfig.apiStatus(Version3) returns "DEPRECATED"
        MockedAppConfig
          .deprecationFor(Version3)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          checkBuildApiStatus(Version3)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }
  }

}
