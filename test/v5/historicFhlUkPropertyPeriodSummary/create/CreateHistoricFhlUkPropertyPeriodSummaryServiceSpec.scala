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

package v5.historicFhlUkPropertyPeriodSummary.create

import common.models.domain.PeriodId
import common.models.errors._
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v5.historicFhlUkPropertyPeriodSummary.create.model.request.{
  Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody,
  Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData
}
import v5.historicFhlUkPropertyPeriodSummary.create.model.response.CreateHistoricFhlUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  private val nino: String                   = "AA123456A"
  private val validPeriodId                  = PeriodId("2021-01-06", "2021-02-06")
  implicit private val correlationId: String = "X-123"

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockedCreateHistoricFhlUkPropertyPeriodSummaryConnector
          .create(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateHistoricFhlUkPropertyPeriodSummaryResponse(validPeriodId)))))

        await(service.create(request)) shouldBe Right(ResponseWrapper(correlationId, CreateHistoricFhlUkPropertyPeriodSummaryResponse(validPeriodId)))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockedCreateHistoricFhlUkPropertyPeriodSummaryConnector
            .create(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.create(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO"            -> NinoFormatError,
        "INVALID_TYPE"            -> InternalError,
        "INVALID_PAYLOAD"         -> InternalError,
        "INVALID_CORRELATIONID"   -> InternalError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "DUPLICATE_SUBMISSION"    -> RuleDuplicateSubmissionError,
        "NOT_ALIGN_PERIOD"        -> RuleMisalignedPeriodError,
        "OVERLAPS_IN_PERIOD"      -> RuleOverlappingPeriodError,
        "NOT_CONTIGUOUS_PERIOD"   -> RuleNotContiguousPeriodError,
        "INVALID_PERIOD"          -> RuleToDateBeforeFromDateError,
        "BOTH_EXPENSES_SUPPLIED"  -> RuleBothExpensesSuppliedError,
        "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
        "SERVER_ERROR"            -> InternalError,
        "SERVICE_UNAVAILABLE"     -> InternalError
      )

      input.foreach(args => (serviceError).tupled(args))
    }
  }

  trait Test extends MockCreateHistoricFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateHistoricFhlUkPropertyPeriodSummaryService(
      connector = mockCreateHistoricFhlUkPropertyPeriodSummaryConnector
    )

    private val body: Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody("2021-01-06", "2021-02-06", None, None)

    protected val request: Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData(Nino(nino), body)

  }

}
