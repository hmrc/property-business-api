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

package v5.historicNonFhlUkPropertyPeriodSummary.retrieve

import common.models.domain.PeriodId
import common.models.errors.PeriodIdFormatError
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v5.historicNonFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData
}
import v5.historicNonFhlUkPropertyPeriodSummary.retrieve.model.response.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  private val nino     = Nino("AA123456A")
  private val from     = "2017-04-06"
  private val to       = "2017-07-04"
  private val periodId = PeriodId(s"${from}_$to")

  implicit private val correlationId: String = "X-123"

  "retrieve" when {
    "service call successful" should {
      "return a valid result" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        private val result = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "map errors according to spec" when {
      def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
        s"the service returns $downStreamErrorCode" in new Test {

          MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode))))))

          private val result = await(service.retrieve(request))
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

      input.foreach(args => (serviceError).tupled(args))
    }
  }

  trait Test extends MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryService(
      connector = mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
    )

    protected val request: RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData(nino, periodId)

    protected val response: RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse(from, to, None, None)

  }

}
