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

package v1.mocks.services

import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.errors.ErrorWrapper
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRequest
import v1.models.response.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod}
import v1.services.ListForeignPropertiesPeriodSummariesService

import scala.concurrent.{ExecutionContext, Future}

trait MockListForeignPropertiesPeriodSummariesService extends MockFactory {

  val mockService: ListForeignPropertiesPeriodSummariesService = mock[ListForeignPropertiesPeriodSummariesService]

  object MockListForeignPropertiesService {

    def listForeignProperties(requestData: ListForeignPropertiesPeriodSummariesRequest)
        : CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]]]]] = {
      (mockService
        .listForeignProperties(_: ListForeignPropertiesPeriodSummariesRequest)(
          _: HeaderCarrier,
          _: ExecutionContext,
          _: EndpointLogContext,
          _: String))
        .expects(requestData, *, *, *, *)
    }

  }

}
