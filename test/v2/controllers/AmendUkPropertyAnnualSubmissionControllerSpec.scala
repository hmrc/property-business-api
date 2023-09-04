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
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetailOld}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockAmendUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockAmendUkPropertyAnnualSubmissionService
import v2.models.request.amendUkPropertyAnnualSubmission._
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty._
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty._
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.common.{Building, FirstYear, StructuredBuildingAllowance}
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendUkPropertyAnnualSubmissionService
    with MockAmendUkPropertyAnnualSubmissionRequestParser
    with MockAuditService
    with MockHateoasFactory {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2022-23"

  "AmendUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAmendUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), hateoasData)
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = None,
          maybeExpectedResponseBody = Some(testHateoasLinksJson),
          maybeAuditResponseBody = Some(testHateoasLinksJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAmendUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, None)
      }

      "the service returns an error" in new Test {
        MockAmendUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, None)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetailOld] {

    private val controller = new AmendUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendUkPropertyAnnualSubmissionRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetailOld] =
      AuditEvent(
        auditType = "CreateAmendUKPropertyAnnualSubmission",
        transactionName = "create-amend-uk-property-annual-submission",
        detail = GenericAuditDetailOld(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Json.obj("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "request" -> requestBodyJson),
          correlationId = correlationId,
          response = auditResponse
        )
      )

    private val ukFhlProperty: UkFhlProperty = UkFhlProperty(
      Some(
        UkFhlPropertyAdjustments(
          Some(1000.20),
          Some(1000.30),
          periodOfGraceAdjustment = true,
          Some(1000.40),
          nonResidentLandlord = true,
          Some(UkPropertyAdjustmentsRentARoom(true))
        )),
      Some(
        UkFhlPropertyAllowances(
          Some(1000.50),
          Some(1000.60),
          Some(1000.70),
          Some(1000.80),
          Some(1000.90),
          None
        ))
    )

    private val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(
      Some(
        UkNonFhlPropertyAdjustments(
          Some(2000.20),
          Some(2000.30),
          Some(2000.40),
          nonResidentLandlord = true,
          Some(UkPropertyAdjustmentsRentARoom(true))
        )),
      Some(
        UkNonFhlPropertyAllowances(
          Some(2000.50),
          Some(2000.60),
          Some(2000.70),
          Some(2000.80),
          Some(2000.90),
          Some(3000.10),
          Some(3000.20),
          None,
          Some(
            Seq(
              StructuredBuildingAllowance(
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
          Some(
            Seq(
              StructuredBuildingAllowance(
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

    private val requestBodyJson = Json.parse(
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

    private val body: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(
      Some(ukFhlProperty),
      Some(ukNonFhlProperty)
    )

    protected val rawData: AmendUkPropertyAnnualSubmissionRawData = AmendUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestBodyJson)

    protected val requestData: AmendUkPropertyAnnualSubmissionRequest =
      AmendUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), body)

    protected val hateoasData: AmendUkPropertyAnnualSubmissionHateoasData = AmendUkPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear)

  }

}
