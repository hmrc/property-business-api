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

package v4.amendForeignPropertyPeriodSummary

import common.models.domain.SubmissionId
import common.models.errors.{RuleDuplicateCountryCodeError, RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import shared.controllers.EndpointLogContext
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.amendForeignPropertyPeriodSummary.model.request.{
  AmendForeignPropertyPeriodSummaryRequestData,
  Def1_AmendForeignPropertyPeriodSummaryRequestBody,
  Def1_AmendForeignPropertyPeriodSummaryRequestData
}

import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  private val nino         = Nino("AA123456A")
  private val businessId   = BusinessId("XAIS12345678910")
  private val taxYear      = TaxYear.fromMtd("2020-21")
  private val submissionId = SubmissionId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  implicit override val correlationId: String = "X-123"

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendForeignPropertyPeriodSummaryConnector
          .amendForeignPropertyPeriodSummary(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendForeignPropertyPeriodSummary(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendForeignPropertyPeriodSummaryConnector
              .amendForeignPropertyPeriodSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendForeignPropertyPeriodSummary(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
          "INVALID_TAX_YEAR"            -> InternalError,
          "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
          "INVALID_SUBMISSION_ID"       -> SubmissionIdFormatError,
          "INVALID_PAYLOAD"             -> InternalError,
          "INVALID_CORRELATIONID"       -> InternalError,
          "NO_DATA_FOUND"               -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> InternalError,
          "DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
          "MISSING_EXPENSES"            -> InternalError,
          "SERVER_ERROR"                -> InternalError,
          "SERVICE_UNAVAILABLE"         -> InternalError
        )

        val extraTysErrors = List(
          "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"       -> InternalError,
          "INCOME_SOURCE_NOT_COMPATIBLE" -> RuleTypeOfBusinessIncorrectError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockAmendForeignPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new AmendForeignPropertyPeriodSummaryService(
      connector = mockAmendForeignPropertyPeriodSummaryConnector
    )

    protected val requestBody: Def1_AmendForeignPropertyPeriodSummaryRequestBody = Def1_AmendForeignPropertyPeriodSummaryRequestBody(None, None)

    protected val request: AmendForeignPropertyPeriodSummaryRequestData =
      Def1_AmendForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, submissionId, requestBody)

  }

}
