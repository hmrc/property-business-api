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

package v6.retrieveUkPropertyCumulativeSummary

import common.models.errors.RuleTypeOfBusinessIncorrectError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.retrieveUkPropertyCumulativeSummary.RetrieveUkPropertyCumulativeSummaryConnector._
import v6.retrieveUkPropertyCumulativeSummary.def1.model.Def1_RetrieveUkPropertyCumulativeSummaryFixture
import v6.retrieveUkPropertyCumulativeSummary.def1.model.request.Def1_RetrieveUkPropertyCumulativeSummaryRequestData
import v6.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData

import scala.concurrent.Future

class RetrieveUkPropertyCumulativeSummaryServiceSpec extends ServiceSpec with Def1_RetrieveUkPropertyCumulativeSummaryFixture {

  implicit override val correlationId: String = "X-123"

  "RetrieveUkPropertyCumulativeSummaryService" when {
    "downstream call is successful" when {
      "a UK response is returned from downstream" must {
        "return a successful result" in new Test {
          MockRetrieveUkPropertyConnector.retrieveUkProperty(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, UkResult(fullResponse))))

          await(service.retrieveUkProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponse))
        }
      }

      "a non-uk result is returned from downstream" must {
        "return a RuleTypeOfBusinessIncorrectError" in new Test {
          MockRetrieveUkPropertyConnector.retrieveUkProperty(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, NonUkResult)))

          await(service.retrieveUkProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
        }
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in new Test {
            MockRetrieveUkPropertyConnector.retrieveUkProperty(requestData) returns
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))

            await(service.retrieveUkProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"    -> InternalError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "NOT_FOUND"                 -> NotFoundError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        errorMap.foreach(args => (serviceError _).tupled(args))
      }
    }

    trait Test extends MockRetrieveUkPropertyCumulativeSummaryConnector {
      val service = new RetrieveUkPropertyCumulativeSummaryService(mockConnector)

      val requestData: RetrieveUkPropertyCumulativeSummaryRequestData =
        Def1_RetrieveUkPropertyCumulativeSummaryRequestData(Nino("AA123456A"), BusinessId("XAIS12345678910"), TaxYear.fromMtd("2025-26"))

    }

  }

}
