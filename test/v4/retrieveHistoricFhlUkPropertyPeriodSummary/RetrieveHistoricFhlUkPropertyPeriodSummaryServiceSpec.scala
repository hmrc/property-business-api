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

package v4.retrieveHistoricFhlUkPropertyPeriodSummary

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.request.{Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData, RetrieveHistoricFhlUkPiePeriodSummaryRequestData}
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.response.{Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse, RetrieveHistoricFhlUkPiePeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  "retrieve" should {
    "return a success result" when {
      "a valid result is found" in new Test {
        MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        val result: ServiceOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse] =
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

          val result: ServiceOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse] =
            await(service.retrieve(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO" -> NinoFormatError,
        "INVALID_TYPE" -> InternalError,
        "INVALID_DATE_FROM" -> PeriodIdFormatError,
        "INVALID_DATE_TO" -> PeriodIdFormatError,
        "NOT_FOUND_PROPERTY" -> NotFoundError,
        "NOT_FOUND_PERIOD" -> NotFoundError,
        "SERVER_ERROR" -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveHistoricFhlUkPropertyPeriodSummaryService(
      connector = mockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
    )

    protected val response: RetrieveHistoricFhlUkPiePeriodSummaryResponse =
      Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse("fromDate", "toDate", None, None)

    protected val request: RetrieveHistoricFhlUkPiePeriodSummaryRequestData =
      Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData(nino, periodId)

  }

}
