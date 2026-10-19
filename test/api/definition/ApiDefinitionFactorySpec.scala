/*
 * Copyright 2026 HM Revenue & Customs
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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIAccessType.{CONTROLLED, PUBLIC}
import api.definition.APIStatus.{ALPHA, BETA}
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

class ApiDefinitionFactorySpec extends UnitSpec with MockAppConfig {

  "set the access level" when {
    "the controlled access flag is enabled" should {
      "return CONTROLLED" in new Test {
        MockedAppConfig.endpointsEnabled(Version6) returns true
        MockedAppConfig.apiStatus(Version6) returns "BETA"
        MockedAppConfig.deprecationFor(Version6).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.controlledAccessEnabled returns true

        apiDefinitionFactory.definition.api.versions.head.access shouldBe CONTROLLED
      }
    }

    "the controlled access flag is disabled" should {
      "return PUBLIC" in new Test {
        MockedAppConfig.endpointsEnabled(Version6) returns true
        MockedAppConfig.apiStatus(Version6) returns "BETA"
        MockedAppConfig.deprecationFor(Version6).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.controlledAccessEnabled returns false

        apiDefinitionFactory.definition.api.versions.head.access shouldBe PUBLIC
      }
    }
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {

        MockedAppConfig.apiStatus(Version6) returns "BETA"
        MockedAppConfig.endpointsEnabled(Version6) returns true
        MockedAppConfig.deprecationFor(Version6).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.controlledAccessEnabled returns false

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Property Business (MTD)",
              description = "An API for providing property business data",
              context = "individuals/business/property",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
                APIVersion(
                  Version6,
                  status = BETA,
                  access = APIAccessType.PUBLIC,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      "return the expected status" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "BETA"
        MockedAppConfig.deprecationFor(Version9).returns(NotDeprecated.valid).anyNumberOfTimes()

        val result: APIStatus = apiDefinitionFactory.buildAPIStatus(Version9)
        result shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present but invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "not-a-status"
        MockedAppConfig.deprecationFor(Version9).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "DEPRECATED"

        MockedAppConfig
          .deprecationFor(Version9)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          apiDefinitionFactory.buildAPIStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }
  }

  class Test extends MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "individuals/business/property"
  }

}
