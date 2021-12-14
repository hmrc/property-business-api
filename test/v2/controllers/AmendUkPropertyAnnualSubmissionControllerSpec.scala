/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockAmendUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockAmendUkPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.hateoas.Method.GET
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyAnnualSubmission._
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty._
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty._
import v2.models.request.common.{Building, FirstYear, StructuredBuildingAllowance}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendUkPropertyAnnualSubmissionService
    with MockAmendUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val taxYear = "2022-23"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendUkPropertyAnnualSubmissionRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLink = Link(href = s"/Individuals/business/property/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |    {
       |      "href":"/individuals/business/property/uk/TC663795B/XAIS12345678910/annual/2022-23",
       |      "method":"PUT",
       |      "rel":"amend-uk-property-annual-submission"
       |    },
       |    {
       |      "href":"/individuals/business/property/uk/TC663795B/XAIS12345678910/annual/2022-23",
       |      "method":"GET",
       |      "rel":"self"
       |    },
       |    {
       |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/annual/2022-23",
       |      "method":"DELETE",
       |      "rel":"delete-property-annual-submission"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  private val requestJson = Json.parse(
    """
      |{
      |  "ukFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 1000.50,
      |      "businessPremisesRenovationAllowance": 1000.60,
      |      "otherCapitalAllowance": 1000.70,
      |      "electricChargePointAllowance": 1000.80,
      |      "zeroEmissionsCarAllowance": 1000.90
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 1000.10,
      |      "privateUseAdjustment": 1000.20,
      |      "balancingCharge": 1000.30,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 2000.50,
      |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |      "businessPremisesRenovationAllowance": 2000.70,
      |      "otherCapitalAllowance": 2000.80,
      |      "costOfReplacingDomesticGoods": 2000.90,
      |      "electricChargePointAllowance": 3000.10,
      |      "structuredBuildingAllowance": [
      |        {
      |          "amount": 3000.30,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.40
      |          },
      |          "building": {
      |            "name": "house name",
      |            "postcode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "enhancedStructuredBuildingAllowance": [
      |        {
      |          "amount": 3000.50,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.60
      |          },
      |          "building": {
      |            "number": "house number",
      |            "postcode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "zeroEmissionsCarAllowance": 3000.20
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 2000.10,
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
  )

  private val ukFhlProperty: UkFhlProperty = UkFhlProperty(
    Some(UkFhlPropertyAdjustments(
      Some(1000.10),
      Some(1000.20),
      Some(1000.30),
      periodOfGraceAdjustment = true,
      Some(1000.40),
      nonResidentLandlord = true,
      Some(UkPropertyAdjustmentsRentARoom(true))
    )),
    Some(UkFhlPropertyAllowances(
      Some(1000.50),
      Some(1000.60),
      Some(1000.70),
      Some(1000.80),
      Some(1000.90),
      None
    ))
  )

  private val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(
    Some(UkNonFhlPropertyAdjustments(
      Some(2000.10),
      Some(2000.20),
      Some(2000.30),
      Some(2000.40),
      nonResidentLandlord = true,
      Some(UkPropertyAdjustmentsRentARoom(true))
    )),
    Some(UkNonFhlPropertyAllowances(
      Some(2000.50),
      Some(2000.60),
      Some(2000.70),
      Some(2000.80),
      Some(2000.90),
      Some(3000.10),
      Some(3000.20),
      None,
      Some(Seq(StructuredBuildingAllowance(
        3000.30,
        Some(FirstYear(
          "2020-01-01",
          3000.40
        )),
        Building(
          Some("house name"),
          None,
          "GF49JH"
        )
      ))),
      Some(Seq(StructuredBuildingAllowance(
        3000.50,
        Some(FirstYear(
          "2020-01-01",
          3000.60
        )),
        Building(
          None,
          Some("house number"),
          "GF49JH"
        )
      )))
    ))
  )

  val body: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(
    Some(ukFhlProperty),
    Some(ukNonFhlProperty)
  )

  private val rawData = AmendUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestJson)
  private val request = AmendUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockAmendUkPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendUkPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RulePropertyIncomeAllowanceError,BAD_REQUEST),
          (ValueFormatError.copy(
            paths = Some(List(
              "/ukFhlProperty/adjustments/lossBroughtForward",
              "/ukFhlProperty/adjustments/balancingCharge",
              "/ukFhlProperty/adjustments/privateUseAdjustment",
              "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
              "/ukFhlProperty/allowances/annualInvestmentAllowance",
              "/ukFhlProperty/allowances/businessPremisesRenovationAllowance",
              "/ukFhlProperty/allowances/otherCapitalAllowance",
              "/ukFhlProperty/allowances/electricChargePointAllowance",
              "/ukFhlProperty/allowances/zeroEmissionsCarAllowance",
              "/ukNonFhlProperty/adjustments/lossBroughtForward",
              "/ukNonFhlProperty/adjustments/balancingCharge",
              "/ukNonFhlProperty/adjustments/privateUseAdjustment",
              "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
              "/ukNonFhlProperty/allowances/annualInvestmentAllowance",
              "/ukNonFhlProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
              "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance",
              "/ukNonFhlProperty/allowances/otherCapitalAllowance",
              "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods",
              "/ukNonFhlProperty/allowances/electricChargePointAllowance",
              "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/amount",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/amount",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
            ))
          ), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleBothAllowancesSuppliedError, BAD_REQUEST),
          (RuleBuildingNameNumberError, BAD_REQUEST),
          (StringFormatError.copy(
            paths = Some(List(
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/name",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/postcode",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"
            ))
          ), BAD_REQUEST),
          (DateFormatError.copy(
            paths = Some(List(
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"
            ))
          ), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockAmendUkPropertyAnnualSubmissionService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RulePropertyIncomeAllowanceError,BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}