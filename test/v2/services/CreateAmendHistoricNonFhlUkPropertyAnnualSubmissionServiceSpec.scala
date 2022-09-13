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

import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.ResponseModelsFixture
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse
import v2.services.RetrieveHistoricNonFhlUkPropertyPeriodSummaryService.downstreamErrorMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec with ResponseModelsFixture {

  trait Test extends MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit val correlationId: String          = "someCorrelationId"

    val service = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService(
      connector = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
    )
  }

  "service" should {
    "service call successful" when {

      "return mapped non-fhl result" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
          .amend(Request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))))

        await(service.amend(Request)) shouldBe Right(
          ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))
      }
    }
  }

  "unsuccessful" should {

    "map non fhl errors according to spec" when {
      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
            .amend(Request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.amend(Request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }
      downstreamErrorMap.foreach(args => (serviceError _).tupled(args))
    }
  }
}
