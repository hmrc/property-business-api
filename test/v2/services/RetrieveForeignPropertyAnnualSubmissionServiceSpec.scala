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
import v2.mocks.connectors.MockRetrieveForeignPropertyAnnualSubmissionConnector
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult}
import v2.controllers.EndpointLogContext
import v2.models.domain.{Nino, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "X-123"

  private val response = RetrieveForeignPropertyAnnualSubmissionResponse(
    "2020-07-07T10:59:47.544Z",
    Some(ForeignFhlEeaEntry(
      Some(ForeignFhlEeaAdjustments(
        Some(100.25),
        Some(100.25),
        Some(true))),
      Some(ForeignFhlEeaAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25))))),
    Some(Seq(ForeignPropertyEntry(
      "GER",
      Some(ForeignPropertyAdjustments(
        Some(100.25),
        Some(100.25))),
      Some(ForeignPropertyAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(Seq(StructuredBuildingAllowance(
          3545.12,
          Some(FirstYear(
            "2020-03-29",
            3453.34
          )),
          Building(
            Some("Building Name"),
            Some("12"),
            "TF3 4GH"
          )
        )))))))))

  private val requestData = RetrieveForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear)

  trait Test extends MockRetrieveForeignPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignPropertyAnnualSubmissionService(
      connector = mockRetrieveForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ForeignResult(response)))))

        await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "a non-foreign result is found" should {
    "return a RULE_TYPE_OF_BUSINESS_INCORRECT error" in new Test {
      MockRetrieveForeignPropertyConnector
        .retrieveForeignProperty(requestData) returns Future.successful(Right(ResponseWrapper(correlationId, NonForeignResult)))

      await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_CORRELATIONID" -> InternalError,
        "NO_DATA_FOUND" -> NotFoundError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR" -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
