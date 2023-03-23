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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
import v2.models.domain.PeriodId
import api.models.errors._
import api.models.ResponseWrapper
import api.models.domain.Nino
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.{PeriodExpenses, PeriodIncome, RetrieveHistoricNonFhlUkPiePeriodSummaryResponse}
import v2.services.RetrieveHistoricNonFhlUkPropertyPeriodSummaryService.downstreamErrorMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val from: String                   = "2017-04-06"
  val to: String                     = "2017-07-04"
  val periodId: String               = s"${from}_$to"
  implicit val correlationId: String = "X-123"

  val periodIncome: PeriodIncome     = PeriodIncome(None, None, None, None, None, None)
  val periodExpenses: PeriodExpenses = PeriodExpenses(None, None, None, None, None, None, None, None, None, None, None)

  private val request  = RetrieveHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))
  private val response = RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(from, to, Some(periodIncome), Some(periodExpenses))

  trait Test extends MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryService(
      connector = mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
    )
  }

  "retrieve" should {
    "service call successful" when {
      "return a valid result" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        private val result = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "map errors according to spec" when {
      def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
        s"a $downStreamErrorCode error is returned from the service" in new Test {

          MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode))))))

          private val result = await(service.retrieve(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      downstreamErrorMap.foreach(args => (serviceError _).tupled(args))
    }
  }
}
