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
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
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
import v3.controllers.validators.MockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory
import v3.models.request.createHistoricFhlUkPiePeriodSummary._
import v3.models.response.createHistoricFhlUkPiePeriodSummary._
import v3.services.MockCreateHistoricFhlUkPiePeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricFhlUkPiePeriodSummaryService
    with MockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId      = "2021-01-01_2021-01-02"
  private val mtdId: String = "test-mtd-id"

  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  def fakeRequestWithBody[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

  val testHateoasLinksJson: JsObject = Json
    .parse("""{
        |  "links": [ { "href":"/some/link", "method":"GET", "rel":"someRel" } ]
        |}
        |""".stripMargin)
    .as[JsObject]

  "CreateHistoricFhlUkPiePeriodSummaryController" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateHistoricFhlUkPiePeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateHistoricFhlUkPiePeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val controller: CreateHistoricFhlUkPiePeriodSummaryController = new CreateHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory,
      service = mockCreateHistoricFhlUkPiePeriodSummaryService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> validNino),
          request = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = Json.parse("""
        |{
        |    "fromDate": "2021-01-01",
        |    "toDate": "2021-01-02"
        |}
        |""".stripMargin)

    private val responseBodyJson: JsValue = Json.parse("""
        |{
        |   "periodId": "2021-01-01_2021-01-02"
        |}
        |""".stripMargin)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson

    protected val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody("2021-01-01", "2021-01-02", None, None)

    protected val requestData: CreateHistoricFhlUkPiePeriodSummaryRequestData =
      CreateHistoricFhlUkPiePeriodSummaryRequestData(Nino(validNino), requestBody)

    protected val hateoasData: CreateHistoricFhlUkPiePeriodSummaryHateoasData =
      CreateHistoricFhlUkPiePeriodSummaryHateoasData(validNino, PeriodId(periodId))

    protected val responseData: CreateHistoricFhlUkPiePeriodSummaryResponse = CreateHistoricFhlUkPiePeriodSummaryResponse(PeriodId(periodId))

  }

}
