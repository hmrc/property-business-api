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

package v4.controllers.retrieveUkPropertyPeriodSummary.def1.model.response

import api.hateoas.Method._
import api.hateoas.{HateoasFactory, HateoasWrapper, Link}
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.controllers.retrieveUkPropertyPeriodSummary.def1.model.Def1_RetrieveUkPropertyPeriodSummaryFixture
import v4.controllers.retrieveUkPropertyPeriodSummary.model.response._

class Def1_RetrieveUkPropertyPeriodSummaryResponseSpec extends UnitSpec with MockAppConfig with Def1_RetrieveUkPropertyPeriodSummaryFixture {
  val downstreamJson: JsValue                        = fullDownstreamJson
  val mtdJson: JsValue                               = fullMtdJson
  val model: RetrieveUkPropertyPeriodSummaryResponse = fullResponseModel

  val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData = RetrieveUkPropertyPeriodSummaryHateoasData(
    nino = "AA999999A",
    businessId = "XAIS12345678910",
    taxYear = "2022-23",
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  "RetrieveUkPropertyPeriodSummaryResponse" when {
    "read from downstream JSON" should {
      "create the expected model" in {
        downstreamJson.as[Def1_RetrieveUkPropertyPeriodSummaryResponse] shouldBe model
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

  "hateoasLinksFactory" when {
    "wrap" should {
      "return the expected wrapped response with correct links" in {
        MockedAppConfig.apiGatewayContext.returns("individuals/business/property").anyNumberOfTimes()
        val wrappedResponse: HateoasWrapper[RetrieveUkPropertyPeriodSummaryResponse] = new HateoasFactory(mockAppConfig).wrap(model, hateoasData)

        val baseUrl = "/individuals/business/property/uk/AA999999A/XAIS12345678910/period/2022-23"

        val expectedWrappedResponse: HateoasWrapper[RetrieveUkPropertyPeriodSummaryResponse] = HateoasWrapper(
          model,
          Seq(
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", PUT, "amend-uk-property-period-summary"),
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", GET, "self"),
            Link("/individuals/business/property/AA999999A/XAIS12345678910/period/2022-23", GET, "list-property-period-summaries")
          )
        )

        wrappedResponse shouldBe expectedWrappedResponse
      }
    }
  }

}
