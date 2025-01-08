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
import common.models.domain.PeriodId
import common.models.errors.RuleMisalignedPeriodError
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method.GET
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.audit.{AuditEvent, AuditResponse}
import shared.models.auth.UserDetails
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v3.controllers.validators.MockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory
import v3.models.request.amendHistoricFhlUkPiePeriodSummary._
import v3.models.response.amendHistoricFhlUkPiePeriodSummary.AmendHistoricFhlUkPropertyPeriodSummaryHateoasData
import v3.services.MockAmendHistoricFhlUkPropertyPeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId      = "somePeriodId"
  private val mtdId: String = "test-mtd-id"

  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

  val testHateoasLinksJson: JsObject = Json
    .parse("""{
        |  "links": [ { "href":"/some/link", "method":"GET", "rel":"someRel" } ]
        |}
        |""".stripMargin)
    .as[JsObject]

  "AmendHistoricFhlUkPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendHistoricFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), hateoasData)
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(testHateoasLinksJson),
          maybeAuditResponseBody = Some(testHateoasLinksJson))
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendHistoricFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTestWithAudit(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val controller = new AmendHistoricFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory,
      service = mockAmendHistoricFhlUkPropertyPeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, periodId)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "AmendHistoricFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "amend-historic-fhl-property-income-expenses-period-summary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> validNino, "periodId" -> periodId),
          request = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = JsObject.empty

    protected val requestBody: AmendHistoricFhlUkPiePeriodSummaryRequestBody = AmendHistoricFhlUkPiePeriodSummaryRequestBody(None, None)

    protected val requestData: AmendHistoricFhlUkPiePeriodSummaryRequestData =
      AmendHistoricFhlUkPiePeriodSummaryRequestData(Nino(validNino), PeriodId(periodId), requestBody)

    protected val hateoasData: AmendHistoricFhlUkPropertyPeriodSummaryHateoasData =
      AmendHistoricFhlUkPropertyPeriodSummaryHateoasData(validNino, periodId)

  }

}
