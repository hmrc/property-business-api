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

package v6.retrieveHistoricFhlUkPropertyAnnualSubmission

import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.def1.model.response._
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request._
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.model.response._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricFhlUkPropertyAnnualSubmissionService
    with MockRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService
    with MockIdGenerator {

  private val mtdTaxYear = "2020-21"
  private val taxYear    = TaxYear.fromMtd(mtdTaxYear)

  "RetrieveHistoricFhlUkPropertyAnnualSubmissionController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveHistoricFhlUkPropertyAnnualSubmissionService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveHistoricFhlUkPropertyAnnualSubmissionService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockRetrieveHistoricFhlUkPropertyAnnualSubmissionService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, mtdTaxYear)(fakeGetRequest)

    protected val requestData: Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(validNino), taxYear)

    private val annualAdjustments: AnnualAdjustments = AnnualAdjustments(
      Some(BigDecimal("100.11")),
      Some(BigDecimal("200.11")),
      Some(BigDecimal("105.11")),
      periodOfGraceAdjustment = true,
      Some(BigDecimal("100.11")),
      nonResidentLandlord = false,
      Some(RentARoom(true))
    )

    private val annualAllowances: AnnualAllowances = AnnualAllowances(
      Some(BigDecimal("100.11")),
      Some(BigDecimal("300.11")),
      Some(BigDecimal("405.11")),
      Some(BigDecimal("550.11"))
    )

    protected val responseData: RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(Some(annualAdjustments), Some(annualAllowances))

    val responseBodyJson: JsObject = Json
      .parse("""
        |{
        | "annualAdjustments": {
        |     "lossBroughtForward":100.11,
        |     "privateUseAdjustment":200.11,
        |     "balancingCharge":105.11,
        |     "periodOfGraceAdjustment":true,
        |     "businessPremisesRenovationAllowanceBalancingCharges":100.11,
        |     "nonResidentLandlord":false,
        |     "rentARoom": {
        |       "jointlyLet":true
        |      }
        |  },
        |  "annualAllowances": {
        |     "annualInvestmentAllowance":100.11,
        |     "businessPremisesRenovationAllowance":300.11,
        |     "otherCapitalAllowance":405.11,
        |     "propertyIncomeAllowance":550.11
        |  }
        |}
        |""".stripMargin)
      .as[JsObject]

  }

}
