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

package v2.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.{ DownstreamOutcome, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector }
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.{ ExecutionContext, Future }

trait MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector extends MockFactory {

  val mockRetrieveHistoricNonFhlUkPropertyConnector: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector =
    mock[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector]

  object MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector {

    def retrieve(requestData: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest)
      : CallHandler[Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]]] = {
      (
        mockRetrieveHistoricNonFhlUkPropertyConnector
          .retrieve(_: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(requestData, *, *, *)
    }
  }
}
