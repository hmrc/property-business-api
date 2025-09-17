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

package v6.historicNonFhlUkPropertyPeriodSummary.amend

import common.models.domain.PeriodId
import common.models.errors.{PeriodIdFormatError, RuleBothExpensesSuppliedError}
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import uk.gov.hmrc.http.HeaderCarrier
import v6.historicNonFhlUkPropertyPeriodSummary.amend.model.request.{
  AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData
}

import scala.concurrent.Future

class AmendHistoricNonFhlUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  implicit override val correlationId: String = "X-123"

  "The service" when {
    "a downstream request is successful" should {
      "return the mapped result" in new Test {
        MockedAmendHistoricNonFhlUkPiePeriodSummaryConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.amend(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "a downstream request returns an error code" should {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"map the $downstreamErrorCode error" in new Test {

          MockedAmendHistoricNonFhlUkPiePeriodSummaryConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.amend(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
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

      input.foreach(args => (serviceError).tupled(args))
    }
  }

  trait Test extends MockAmendHistoricNonFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new AmendHistoricNonFhlUkPropertyPeriodSummaryService(
      connector = mockConnector
    )

    protected val requestBody: Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(None, None)

    protected val request: AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData(nino, periodId, requestBody)

  }

}
