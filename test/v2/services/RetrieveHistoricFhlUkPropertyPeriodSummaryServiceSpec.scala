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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
import v2.models.domain.{Nino, PeriodId}
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary.{PeriodExpenses, PeriodIncome, RetrieveHistoricFhlUkPiePeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val periodId = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-123"

  val periodExpenses: PeriodExpenses = PeriodExpenses(None, None, None, None, None, None, None, None, None)
  val periodIncome: PeriodIncome = PeriodIncome(None, None , None)

  private val request = RetrieveHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))
  private val response = RetrieveHistoricFhlUkPiePeriodSummaryResponse("2017-04-06","2017-07-04", Some(periodIncome), Some(periodExpenses))


  trait Test extends MockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveHistoricFhlUkPropertyPeriodSummaryService(
      connector = mockRetrieveHistoricFhlUkPropertyPeriodSummaryConnector
    )
  }

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

      val input = Seq(
        "INVALID_NINO"        -> NinoFormatError,
        "INVALID_DATE_FROM"   -> PeriodIdFormatError,
        "INVALID_DATE_TO"   -> PeriodIdFormatError,
        "INVALID_CORRELATION_ID"    -> UnauthorisedError,
        "INVALID_TYPE"    -> InternalError,
        "NOT_FOUND_PROPERTY"  -> NotFoundError,
        "NOT_FOUND_PERIOD"    -> NotFoundError,
        "SERVER_ERROR"        -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
