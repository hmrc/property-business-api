/*
 * Copyright 2022 HM Revenue & Customs
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
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockAmendHistoricFhlUkPiePeriodSummaryConnector
import v2.models.domain.{ Nino, PeriodId }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendHistoricFhlUkPiePeriodSummary.{
  AmendHistoricFhlUkPiePeriodSummaryRequest,
  AmendHistoricFhlUkPiePeriodSummaryRequestBody
}

import scala.concurrent.Future

class AmendHistoricFhlUkPiePeriodSummaryServiceSpec extends ServiceSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  private val requestBody = AmendHistoricFhlUkPiePeriodSummaryRequestBody(None, None)
  private val request     = AmendHistoricFhlUkPiePeriodSummaryRequest(nino, periodId, requestBody)

  implicit val correlationId: String = "X-123"

  "The service" when {
    "a downstream request is successful" should {
      "return the mapped result" in new Test {
        MockAmendHistoricFhlUkPiePeriodSummaryConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.amend(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "a downstream request returns an error code" should {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"map the $ifsErrorCode error" in new Test {

          MockAmendHistoricFhlUkPiePeriodSummaryConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.amend(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_NINO"                -> NinoFormatError,
        "INVALID_TYPE"                -> InternalError,
        "INVALID_PAYLOAD"             -> InternalError,
        "INVALID_DATE_FROM"           -> PeriodIdFormatError,
        "INVALID_DATE_TO"             -> PeriodIdFormatError,
        "INVALID_CORRELATIONID"       -> InternalError,
        "SUBMISSION_PERIOD_NOT_FOUND" -> NotFoundError,
        "NOT_FOUND_PROPERTY"          -> NotFoundError,
        "NOT_FOUND_INCOME_SOURCE"     -> NotFoundError,
        "NOT_FOUND"                   -> NotFoundError,
        "BOTH_EXPENSES_SUPPLIED"      -> RuleBothExpensesSuppliedError,
        "SERVER_ERROR"                -> InternalError,
        "SERVICE_UNAVAILABLE"         -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))

    }
  }

  trait Test extends MockAmendHistoricFhlUkPiePeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendHistoricFhlUkPiePeriodSummaryService(
      connector = mockConnector
    )
  }

}
