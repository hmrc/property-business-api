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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.MockIdGenerator
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.hateoas._
import api.models.hateoas.Link
import api.models.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockRetrieveForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services.MockRetrieveForeignPropertyPeriodSummaryService
import v2.models.request.retrieveForeignPropertyPeriodSummary._
import v2.models.response.retrieveForeignPropertyPeriodSummary._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyPeriodSummaryService
    with MockRetrieveForeignPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2022-23"

  "Retrieve Foreign property period summary" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {

        MockRetrieveForeignPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(
            responseBody,
            RetrieveForeignPropertyPeriodSummaryHateoasData(nino = nino, businessId = businessId, submissionId = submissionId, taxYear = taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val expectedResponseBody: JsValue = Json.toJson(HateoasWrapper(responseBody, Seq(testHateoasLink)))
        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(expectedResponseBody))
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveForeignPropertyRequestParser,
      service = mockRetrieveForeignPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] =
      controller.handleRequest(nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId)(fakeGetRequest)

    protected val rawData: RetrieveForeignPropertyPeriodSummaryRawData =
      RetrieveForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId)

    protected val requestData: RetrieveForeignPropertyPeriodSummaryRequest =
      RetrieveForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId)

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val responseBody: RetrieveForeignPropertyPeriodSummaryResponse = RetrieveForeignPropertyPeriodSummaryResponse(
      submittedOn = "",
      fromDate = "",
      toDate = "",
      foreignFhlEea = Some(
        ForeignFhlEea(
          income = Some(
            ForeignFhlEeaIncome(
              rentAmount = Some(3426.34)
            )),
          expenses = Some(ForeignFhlEeaExpenses(
            premisesRunningCosts = Some(1000.12),
            repairsAndMaintenance = Some(1000.12),
            financialCosts = Some(1000.12),
            professionalFees = Some(1000.12),
            costOfServices = Some(1000.12),
            travelCosts = Some(1000.12),
            other = Some(1000.12),
            consolidatedExpenses = None
          ))
        )),
      foreignNonFhlProperty = Some(
        Seq(
          ForeignNonFhlProperty(
            countryCode = "ZZZ",
            income = Some(
              ForeignNonFhlPropertyIncome(
                rentIncome = Some(
                  ForeignNonFhlPropertyRentIncome(
                    rentAmount = Some(1000.12)
                  )),
                foreignTaxCreditRelief = true,
                premiumsOfLeaseGrant = Some(1000.12),
                otherPropertyIncome = Some(1000.12),
                foreignTaxPaidOrDeducted = Some(1000.12),
                specialWithholdingTaxOrUkTaxPaid = Some(1000.12)
              )),
            expenses = Some(
              ForeignNonFhlPropertyExpenses(
                premisesRunningCosts = Some(1000.12),
                repairsAndMaintenance = Some(1000.12),
                financialCosts = Some(1000.12),
                professionalFees = Some(1000.12),
                costOfServices = Some(1000.12),
                travelCosts = Some(1000.12),
                residentialFinancialCost = Some(1000.12),
                broughtFwdResidentialFinancialCost = Some(1000.12),
                other = Some(1000.12),
                consolidatedExpenses = None
              ))
          )
        )
      )
    )

  }

}
