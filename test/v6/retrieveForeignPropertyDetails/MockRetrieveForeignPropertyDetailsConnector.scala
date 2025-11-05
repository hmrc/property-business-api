/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyDetails

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v6.retrieveForeignPropertyDetails.model.Result
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveForeignPropertyDetailsConnector extends TestSuite with MockFactory {

  val mockConnector: RetrieveForeignPropertyDetailsConnector = mock[RetrieveForeignPropertyDetailsConnector]

  object MockRetrieveForeignPropertyConnector {

    def retrieveForeignPropertyDetails(requestData: RetrieveForeignPropertyDetailsRequestData): CallHandler[Future[DownstreamOutcome[Result]]] =
      (
        mockConnector
          .retrieveForeignPropertyDetails(_: RetrieveForeignPropertyDetailsRequestData)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(requestData, *, *, *)

  }

}
