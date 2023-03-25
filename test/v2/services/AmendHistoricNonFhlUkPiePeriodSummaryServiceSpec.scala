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
import v2.mocks.connectors.MockAmendHistoricNonFhlUkPiePeriodSummaryConnector
import v2.models.domain.PeriodId
import api.models.errors._
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.{AmendHistoricNonFhlUkPiePeriodSummaryRequest, AmendHistoricNonFhlUkPiePeriodSummaryRequestBody}
import v2.services.AmendHistoricNonFhlUkPiePeriodSummaryService.downstreamErrorMap

import scala.concurrent.Future

class AmendHistoricNonFhlUkPiePeriodSummaryServiceSpec extends ServiceSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  private val requestBody = AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(None, None)
  private val request     = AmendHistoricNonFhlUkPiePeriodSummaryRequest(nino, periodId, requestBody)

  implicit val correlationId: String = "X-123"

  "The service" when {
    "a downstream request is successful" should {
      "return the mapped result" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.amend(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "a downstream request returns an error code" should {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"map the $ifsErrorCode error" in new Test {

          MockAmendHistoricNonFhlUkPiePeriodSummaryConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.amend(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      downstreamErrorMap.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockAmendHistoricNonFhlUkPiePeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendHistoricNonFhlUkPiePeriodSummaryService(
      connector = mockConnector
    )
  }

}
