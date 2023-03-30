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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission._
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val taxYear = "2020-21"

  "RetrieveHistoricNonFhlUkPropertyAnnualSubmissionController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)

    protected val rawData: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRawData =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear)

    protected val requestData: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear))

    private val annualAdjustments = AnnualAdjustments(
      lossBroughtForward = Some(BigDecimal("200.00")),
      balancingCharge = Some(BigDecimal("300.00")),
      privateUseAdjustment = Some(BigDecimal("400.00")),
      businessPremisesRenovationAllowanceBalancingCharges = Some(BigDecimal("80.02")),
      nonResidentLandlord = true,
      rentARoom = Option(RentARoom(jointlyLet = true))
    )

    private val annualAllowances = AnnualAllowances(
      annualInvestmentAllowance = Some(BigDecimal("200.00")),
      otherCapitalAllowance = Some(BigDecimal("300.00")),
      zeroEmissionGoodsVehicleAllowance = Some(BigDecimal("400.00")),
      businessPremisesRenovationAllowance = Some(BigDecimal("200.00")),
      costOfReplacingDomesticGoods = Some(BigDecimal("200.00")),
      propertyIncomeAllowance = Some(BigDecimal("30.02"))
    )

    protected val responseData: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(Some(annualAdjustments), Some(annualAllowances))

    protected val hateoasData: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear)

    private val responseBodyJson: JsValue = Json.parse("""
                                                         |{
                                                         |  "annualAdjustments": {
                                                         |      "lossBroughtForward": 200,
                                                         |      "balancingCharge": 300,
                                                         |      "privateUseAdjustment": 400,
                                                         |      "businessPremisesRenovationAllowanceBalancingCharges":80.02,
                                                         |      "nonResidentLandlord": true,
                                                         |      "rentARoom": {
                                                         |        "jointlyLet": true
                                                         |      }
                                                         |  },
                                                         |  "annualAllowances": {
                                                         |    "annualInvestmentAllowance": 200,
                                                         |    "otherCapitalAllowance": 300,
                                                         |    "zeroEmissionGoodsVehicleAllowance": 400,
                                                         |    "businessPremisesRenovationAllowance": 200,
                                                         |    "costOfReplacingDomesticGoods": 200,
                                                         |    "propertyIncomeAllowance": 30.02
                                                         |  }
                                                         |}
                                                         |""".stripMargin)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson
  }

}
