/*
 * Copyright 2024 HM Revenue & Customs
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

package v6.historicNonFhlUkPropertyPeriodSummary.list

import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v6.historicNonFhlUkPropertyPeriodSummary.list.model.request.ListHistoricNonFhlUkPropertyPeriodSummariesRequestData
import v6.historicNonFhlUkPropertyPeriodSummary.list.model.response.ListHistoricNonFhlUkPropertyPeriodSummariesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockListHistoricNonFhlUkPropertyPeriodSummariesService extends MockFactory {

  val mockService: ListHistoricNonFhlUkPropertyPeriodSummariesService = mock[ListHistoricNonFhlUkPropertyPeriodSummariesService]

  object MockedListHistoricNonFhlUkPropertyPeriodSummariesService {

    def listPeriodSummaries(requestData: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData)
        : CallHandler[Future[ServiceOutcome[ListHistoricNonFhlUkPropertyPeriodSummariesResponse]]] = {
      (
        mockService
          .listPeriodSummaries(_: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
