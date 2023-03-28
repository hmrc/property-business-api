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

package v2.services

import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateUkPropertyPeriodSummaryConnector
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  val businessId: String             = "XAIS12345678910"
  val nino: String                   = "AA123456A"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "X-123"

  private val regularExpensesBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    None,
    None
  )

  val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val regularExpensesRequestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, regularExpensesBody)

  trait Test extends MockCreateUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateUkPropertyPeriodSummaryService(
      connector = mockCreateUkPropertyConnector
    )
  }

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockCreateUkPropertyConnector
          .createUkProperty(regularExpensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createUkProperty(regularExpensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockCreateUkPropertyConnector
              .createUkProperty(regularExpensesRequestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.createUkProperty(regularExpensesRequestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
          "INVALID_PAYLOAD"           -> InternalError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
          "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmissionError,
          "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
          "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
          "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
          "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError,
          "MISSING_EXPENSES"          -> InternalError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors = Seq(
          "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"  -> InternalError,
          "PERIOD_NOT_ALIGNED"      -> RuleMisalignedPeriodError,
          "PERIOD_OVERLAPS"         -> RuleOverlappingPeriodError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
