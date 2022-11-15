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

package v1.models.response.retrieveForeignPropertyAnnualSubmission

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.hateoas.{ Link, Method }
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea.{
  ForeignFhlEeaAdjustments,
  ForeignFhlEeaAllowances,
  ForeignFhlEeaEntry
}
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty.{
  ForeignPropertyAdjustments,
  ForeignPropertyAllowances,
  ForeignPropertyEntry
}

class RetrieveForeignPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  private val retrieveForeignPropertyAnnualSubmissionRequestBody = RetrieveForeignPropertyAnnualSubmissionResponse(
    Some(
      ForeignFhlEeaEntry(
        Some(ForeignFhlEeaAdjustments(Some(100.25), Some(100.25), Some(true))),
        Some(ForeignFhlEeaAllowances(Some(100.25), Some(100.25), Some(100.25), Some(100.25)))
      )),
    Some(
      Seq(ForeignPropertyEntry(
        "GER",
        Some(ForeignPropertyAdjustments(Some(100.25), Some(100.25))),
        Some(ForeignPropertyAllowances(Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25)))
      )))
  )

  private val retrieveForeignPropertyAnnualSubmissionRequestBodyMinimum = RetrieveForeignPropertyAnnualSubmissionResponse(
    None,
    Some(
      Seq(
        ForeignPropertyEntry(
          "GER",
          None,
          None
        )))
  )

  private val jsonBody = Json.parse(
    """
       |{
       |   "foreignFhlEea":
       |      {
       |         "adjustments":{
       |            "privateUseAdjustment":100.25,
       |            "balancingCharge":100.25,
       |            "periodOfGraceAdjustment":true
       |         },
       |         "allowances":{
       |            "annualInvestmentAllowance":100.25,
       |            "otherCapitalAllowance":100.25,
       |            "propertyAllowance":100.25,
       |            "electricChargePointAllowance":100.25
       |         }
       |      },
       |   "foreignProperty":[
       |      {
       |         "countryCode":"GER",
       |         "adjustments":{
       |            "privateUseAdjustment":100.25,
       |            "balancingCharge":100.25
       |         },
       |         "allowances":{
       |            "annualInvestmentAllowance":100.25,
       |            "costOfReplacingDomesticItems":100.25,
       |            "zeroEmissionsGoodsVehicleAllowance":100.25,
       |            "propertyAllowance":100.25,
       |            "otherCapitalAllowance":100.25,
       |            "structureAndBuildingAllowance":100.25,
       |            "electricChargePointAllowance":100.25
       |         }
       |      }
       |   ]
       |}
     """.stripMargin
  )

  private val jsonBodyMinimum = Json.parse(
    """
       |{
       |   "foreignProperty":[
       |      {
       |         "countryCode":"GER"
       |      }
       |   ]
       |}
     """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[RetrieveForeignPropertyAnnualSubmissionResponse] shouldBe retrieveForeignPropertyAnnualSubmissionRequestBody
      }
      "return a valid model with minimum fields" in {
        jsonBodyMinimum.as[RetrieveForeignPropertyAnnualSubmissionResponse] shouldBe retrieveForeignPropertyAnnualSubmissionRequestBodyMinimum
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(retrieveForeignPropertyAnnualSubmissionRequestBody) shouldBe jsonBody
      }
      "return a valid minimum JSON" in {
        Json.toJson(retrieveForeignPropertyAnnualSubmissionRequestBodyMinimum) shouldBe jsonBodyMinimum
      }
    }
  }

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: RetrieveForeignPropertyAnnualSubmissionHateoasData =
          RetrieveForeignPropertyAnnualSubmissionHateoasData("myNino", "myBusinessId", "mySubmissionId")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveForeignPropertyAnnualSubmissionResponse.RetrieveAnnualSubmissionLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(href = s"/my/context/${data.nino}/${data.businessId}/annual/${data.taxYear}",
               method = Method.PUT,
               rel = "amend-property-annual-submission"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/annual/${data.taxYear}", method = Method.GET, rel = "self"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/annual/${data.taxYear}",
               method = Method.DELETE,
               rel = "delete-property-annual-submission")
        )
      }
    }
  }
}
