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

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequestData
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId("2017-04-06_2017-07-04")

  implicit private val correlationId: String = "X-123"

  "retrieve" should {
    "service call successful" when {
      "return a valid result" in new Test {
        MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        private val result = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "map errors according to spec" when {
      def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
        s"a $downStreamErrorCode error is returned from the service" in new Test {

          MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode))))))

          private val result = await(service.retrieve(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO"        -> NinoFormatError,
        "INVALID_DATE_FROM"   -> PeriodIdFormatError,
        "INVALID_DATE_TO"     -> PeriodIdFormatError,
        "INVALID_TYPE"        -> InternalError,
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

    protected val request: RetrieveHistoricFhlUkPiePeriodSummaryRequestData =
      RetrieveHistoricFhlUkPiePeriodSummaryRequestData(nino, periodId)

    protected val response: RetrieveHistoricFhlUkPiePeriodSummaryResponse =
      RetrieveHistoricFhlUkPiePeriodSummaryResponse("2017-04-06", "2017-07-04", None, None)

  }

}
