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
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector.{ ForeignResult, NonForeignResult }
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveForeignPropertyPeriodSummaryConnector
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
import v2.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  val countryCode: String = "FRA"

  val foreignFhlEea: ForeignFhlEea =
    ForeignFhlEea(
      Some(ForeignFhlEeaIncome(Some(5000.99))),
      Some(
        ForeignFhlEeaExpenses(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          None
        ))
    )

  val foreignNonFhlProperty: ForeignNonFhlProperty =
    ForeignNonFhlProperty(
      countryCode,
      Some(
        ForeignNonFhlPropertyIncome(
          Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
          foreignTaxCreditRelief = false,
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99)
        )),
      Some(
        ForeignNonFhlPropertyExpenses(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          None
        ))
    )

  private val response =
    RetrieveForeignPropertyPeriodSummaryResponse("2020-06-17T10:53:38Z",
                                                 "2019-01-29",
                                                 "2020-03-29",
                                                 Some(foreignFhlEea),
                                                 Some(Seq(foreignNonFhlProperty)))

  private val requestData = RetrieveForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, submissionId)

  trait Test extends MockRetrieveForeignPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignPropertyPeriodSummaryService(
      connector = mockRetrieveForeignPropertyConnector
    )
  }

  "The service" should {
    "map the result" when {
      "the downstream response is successful" in new Test {
        MockRetrieveForeignPropertyConnector
          .retrieveForeignProperty(requestData)
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

  "The service" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignPropertyConnector
            .retrieveForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
        "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError,
      )

      val extraTysErrors = List(
        "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
        "INVALID_CORRELATION_ID"  -> InternalError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }
}
