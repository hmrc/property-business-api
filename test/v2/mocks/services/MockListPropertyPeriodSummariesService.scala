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

package v2.mocks.services

import api.controllers.RequestContext
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequestData
import v2.models.response.listPropertyPeriodSummaries.ListPropertyPeriodSummariesResponse
import v2.services.ListPropertyPeriodSummariesService

import scala.concurrent.{ExecutionContext, Future}

trait MockListPropertyPeriodSummariesService extends MockFactory {

  val mockService: ListPropertyPeriodSummariesService = mock[ListPropertyPeriodSummariesService]

  object MockListPropertyPeriodSummariesService {

    def listPeriodSummaries(
        requestData: ListPropertyPeriodSummariesRequestData): CallHandler[Future[ServiceOutcome[ListPropertyPeriodSummariesResponse]]] = {
      (mockService
        .listPeriodSummaries(_: ListPropertyPeriodSummariesRequestData)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)
    }

  }

}
