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

package v4.historicFhlUkPropertyPeriodSummary.retrieve

import common.models.domain.PeriodId
import common.models.errors.PeriodIdFormatError
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceOutcome
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData
}
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.response.{
  Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse,
  RetrieveHistoricFhlUkPropertyPeriodSummaryResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")
  private val periodId                       = PeriodId(from = "2017-04-06", to = "2017-07-04")

  "retrieve" should {
    "return a success result" when {
      "a valid result is found" in new Test {
        MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        val result: ServiceOutcome[RetrieveHistoricFhlUkPropertyPeriodSummaryResponse] =
          await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return relevant mtd error according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveHistoricFhlUkPropertyPeriodSummaryResponse] =
            await(service.retrieve(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO"        -> NinoFormatError,
        "INVALID_TYPE"        -> InternalError,
        "INVALID_DATE_FROM"   -> PeriodIdFormatError,
        "INVALID_DATE_TO"     -> PeriodIdFormatError,
        "NOT_FOUND_PROPERTY"  -> NotFoundError,
        "NOT_FOUND_PERIOD"    -> NotFoundError,
        "SERVER_ERROR"        -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveHistoricFhlUkPropertyPeriodSummaryService(
      connector = mockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
    )

    protected val response: RetrieveHistoricFhlUkPropertyPeriodSummaryResponse =
      Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse("fromDate", "toDate", None, None)

    protected val request: RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData(nino, periodId)

  }

}
