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

package v6.retrieveForeignPropertyCumulativeSummary

import common.models.errors.RuleTypeOfBusinessIncorrectError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.retrieveForeignPropertyCumulativeSummary.model.{ForeignResult, NonForeignResult, Result}
import v6.retrieveForeignPropertyCumulativeSummary.def1.model.Def1_RetrieveForeignPropertyCumulativeSummaryFixture
import v6.retrieveForeignPropertyCumulativeSummary.def1.model.request.Def1_RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData

import scala.concurrent.Future

class RetrieveForeignPropertyCumulativeSummaryServiceSpec extends ServiceSpec with Def1_RetrieveForeignPropertyCumulativeSummaryFixture {

  implicit override val correlationId: String = "X-123"

  "RetrieveForeignPropertyCumulativeSummaryService" when {
    "downstream call is successful" when {
      "a foreign response is returned from downstream" must {
        "return a successful result" in new Test {
          MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, ForeignResult(fullResponse))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponse))
        }
      }

      "a non-foreign result is returned from downstream" must {
        "return a RuleTypeOfBusinessIncorrectError" in new Test {
          MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, NonForeignResult)))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
        }
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in new Test {
            MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData) returns
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))

            await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
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

    trait Test extends MockRetrieveForeignPropertyCumulativeSummaryConnector {
      val service = new RetrieveForeignPropertyCumulativeSummaryService(mockConnector)

      val requestData: RetrieveForeignPropertyCumulativeSummaryRequestData =
        Def1_RetrieveForeignPropertyCumulativeSummaryRequestData(Nino("AA123456A"), BusinessId("XAIS12345678910"), TaxYear.fromMtd("2025-26"))

    }

  }

}
