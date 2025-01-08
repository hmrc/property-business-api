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

package v3.controllers

import common.models.audit.FlattenedGenericAuditDetail
import common.models.errors.RuleMisalignedPeriodError
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.audit.{AuditEvent, AuditResponse}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import shared.hateoas.Method.GET
import shared.utils.MockIdGenerator
import v3.controllers.validators.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
import v3.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission._
import v3.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission._
import v3.services.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockAuditService
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockHateoasFactory
    with MockIdGenerator {

  private val taxYear                            = "2022-23"
  private val transactionReference               = Some("transaction reference")
  private val mtdId: String                      = "test-mtd-id"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

  val testHateoasLinksJson: JsObject = Json
    .parse("""{
        |  "links": [ { "href":"/some/link", "method":"GET", "rel":"someRel" } ]
        |}
        |""".stripMargin)
    .as[JsObject]

  "CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(testHateoasLinksJson))
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val controller = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendHistoricNonFhlPropertyBusinessAnnualSubmission",
        transactionName = "create-and-amend-historic-non-fhl-property-business-annual-submission",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          request = Some(validMtdJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = JsObject.empty

    protected val requestBody: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val requestData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(validNino), TaxYear.fromMtd(taxYear), requestBody)

    protected val hateoasData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(validNino, taxYear)

    protected val responseData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(transactionReference)

    protected val validMtdJson: JsValue = Json.parse("""
     |{
     |   "annualAdjustments": {
     |      "lossBroughtForward": 100.00,
     |      "privateUseAdjustment": 200.00,
     |      "balancingCharge": 300.00,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   },
     |   "annualAllowances": {
     |      "annualInvestmentAllowance": 500.00,
     |      "zeroEmissionGoodsVehicleAllowance": 600.00,
     |      "businessPremisesRenovationAllowance": 700.00,
     |      "otherCapitalAllowance": 800.00,
     |      "costOfReplacingDomesticGoods": 900.00,
     |      "propertyIncomeAllowance": 1000.00
     |   }
     |}
     |""".stripMargin)

  }

}
