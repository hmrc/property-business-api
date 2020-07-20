/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendForeignPropertyAnnualSubmissionRequestParser
import v1.mocks.services.{MockAmendForeignPropertyAnnualSubmissionService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.hateoas.Link
import v1.models.hateoas.Method.GET

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AmendForeignPropertyAnnualSubmissionControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyAnnualSubmissionService
    with MockAmendForeignPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyAnnualSubmissionRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val testHateoasLink = Link(href = s"Individuals/business/property/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

  private val requestJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25,
      |        "periodOfGraceAdjustment":true
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    },
      |  "foreignProperty": [
      |    {
      |      "countryCode":"GER",
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "costOfReplacingDomesticItems":100.25,
      |        "zeroEmissionsGoodsVehicleAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "structureAndBuildingAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )
}
