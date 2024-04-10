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

package v4.retrieveForeignPropertyPeriodSummary

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.Method.GET
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.domain._
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockIdGenerator
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v4.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenses, ForeignFhlEeaIncome}
import v4.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignNonFhlProperty._
import v4.retrieveForeignPropertyPeriodSummary.model.request.{Def1_RetrieveForeignPropertyPeriodSummaryRequestData, RetrieveForeignPropertyPeriodSummaryRequestData}
import v4.retrieveForeignPropertyPeriodSummary.model.response.{Def1_RetrieveForeignPropertyPeriodSummaryResponse, RetrieveForeignPropertyPeriodSummaryHateoasData, RetrieveForeignPropertyPeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyPeriodSummaryService
    with MockRetrieveForeignPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with MockIdGenerator {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2022-23"

  "RetrieveForeignPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(
            responseBody,
            RetrieveForeignPropertyPeriodSummaryHateoasData(nino = nino, businessId = businessId, submissionId = submissionId, taxYear = taxYear))
          .returns(HateoasWrapper(responseBody, testHateoasLinks))

        val expectedResponseBody: JsValue = Json.toJson(HateoasWrapper(responseBody, testHateoasLinks))
        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(expectedResponseBody))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(expectedError = NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(expectedError = RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new RetrieveForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveForeignPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] =
      controller.handleRequest(nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId)(fakeGetRequest)

    protected val requestData: RetrieveForeignPropertyPeriodSummaryRequestData =
      Def1_RetrieveForeignPropertyPeriodSummaryRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), SubmissionId(submissionId))

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val responseBody: RetrieveForeignPropertyPeriodSummaryResponse = Def1_RetrieveForeignPropertyPeriodSummaryResponse(
      submittedOn = Timestamp("2022-06-17T10:53:38Z"),
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
