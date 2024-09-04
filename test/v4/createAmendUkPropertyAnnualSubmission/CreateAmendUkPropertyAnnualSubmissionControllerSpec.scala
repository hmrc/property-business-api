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

package v4.createAmendUkPropertyAnnualSubmission

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty._
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukNonFhlProperty._
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukPropertyRentARoom.Def1_Create_Amend_UkPropertyAdjustmentsRentARoom
import v4.createAmendUkPropertyAnnualSubmission.model.request._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockCreateAmendUkPropertyAnnualSubmissionService
    with MockCreateAmendUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2022-23"

  "CreateAmendUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new CreateAmendUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendUkPropertyAnnualSubmissionValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendUKPropertyAnnualSubmission",
        transactionName = "create-amend-uk-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val ukFhlProperty: Def1_Create_Amend_UkFhlProperty = Def1_Create_Amend_UkFhlProperty(
      Some(
        Def1_Create_Amend_UkFhlPropertyAdjustments(
          Some(1000.20),
          Some(1000.30),
          periodOfGraceAdjustment = true,
          Some(1000.40),
          nonResidentLandlord = true,
          Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true))
        )),
      Some(
        Def1_Create_Amend_UkFhlPropertyAllowances(
          Some(1000.50),
          Some(1000.60),
          Some(1000.70),
          Some(1000.80),
          Some(1000.90),
          None
        ))
    )

    private val ukNonFhlProperty: Def1_Create_Amend_UkNonFhlProperty = Def1_Create_Amend_UkNonFhlProperty(
      Some(
        Def1_Create_Amend_UkNonFhlPropertyAdjustments(
          Some(2000.20),
          Some(2000.30),
          Some(2000.40),
          nonResidentLandlord = true,
          Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true))
        )),
      Some(
        Def1_Create_Amend_UkNonFhlPropertyAllowances(
          Some(2000.50),
          Some(2000.60),
          Some(2000.70),
          Some(2000.80),
          Some(2000.90),
          Some(3000.10),
          Some(3000.20),
          None,
          Some(
            List(
              Def1_Create_Amend_StructuredBuildingAllowance(
                3000.30,
                Some(Def1_Create_Amend_FirstYear(
                  "2020-01-01",
                  3000.40
                )),
                Def1_Create_Amend_Building(
                  Some("house name"),
                  None,
                  "GF49JH"
                )
              ))),
          Some(
            List(
              Def1_Create_Amend_StructuredBuildingAllowance(
                3000.50,
                Some(Def1_Create_Amend_FirstYear(
                  "2020-01-01",
                  3000.60
                )),
                Def1_Create_Amend_Building(
                  None,
                  Some("house number"),
                  "GF49JH"
                )
              )))
        ))
    )

    protected val requestBodyJson: JsValue = Json.parse(
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

    private val body: Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(
      Some(ukFhlProperty),
      Some(ukNonFhlProperty)
    )

    protected val requestData: Def1_CreateAmendUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), body)

  }

}
