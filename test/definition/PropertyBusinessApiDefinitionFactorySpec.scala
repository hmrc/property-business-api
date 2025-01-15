/*
 * Copyright 2025 HM Revenue & Customs
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
import shared.config.Deprecation.NotDeprecated
import shared.config.MockSharedAppConfig
import shared.definition.APIStatus.BETA
import shared.definition._
import shared.mocks.MockHttpClient
import shared.routing._
import shared.utils.UnitSpec

class PropertyBusinessApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockSharedAppConfig {
    val apiDefinitionFactory = new PropertyBusinessApiDefinitionFactory(mockSharedAppConfig)
    MockedSharedAppConfig.apiGatewayContext returns "individuals/business/property"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockedSharedAppConfig.deprecationFor(Version4).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedSharedAppConfig.deprecationFor(Version5).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedSharedAppConfig.deprecationFor(Version6).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedSharedAppConfig.apiStatus(Version4) returns "BETA"
        MockedSharedAppConfig.apiStatus(Version5) returns "BETA"
        MockedSharedAppConfig.apiStatus(Version6) returns "BETA"
        MockedSharedAppConfig.endpointsEnabled(Version4).returns(true).anyNumberOfTimes()
        MockedSharedAppConfig.endpointsEnabled(Version5).returns(true).anyNumberOfTimes()
        MockedSharedAppConfig.endpointsEnabled(Version6).returns(true).anyNumberOfTimes()

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Property Business (MTD)",
              description = "An API for providing property business data",
              context = "individuals/business/property",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
                APIVersion(
                  version = Version4,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version5,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version6,
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

}
