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

import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.retrieveForeignPropertyDetails.def1.model.Def1_RetrieveForeignPropertyDetailsFixture
import v6.retrieveForeignPropertyDetails.def1.model.request.Def1_RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData
import scala.concurrent.Future

class RetrieveForeignPropertyDetailsServiceSpec extends ServiceSpec with Def1_RetrieveForeignPropertyDetailsFixture {

  implicit override val correlationId: String = "X-123"

  "RetrieveForeignPropertyDetailsService" when {
    "downstream call is successful" when {
      "using schema Def1" in new Test {
        MockRetrieveForeignPropertyDetailsConnector
          .retrieveForeignPropertyDetails(requestData)
          .returns(
            Future.successful(Right(ResponseWrapper(correlationId, response)))
          )

        await(service.retrieveForeignPropertyDetails(requestData)).shouldBe(
          Right(ResponseWrapper(correlationId, fullResponse))
        )
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in new Test {
            MockRetrieveForeignPropertyDetailsConnector
              .retrieveForeignPropertyDetails(requestData)
              .returns(
                Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))
              )

            await(service.retrieveForeignPropertyDetails(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap = List(
          "1215" -> NinoFormatError,
          "1007" -> BusinessIdFormatError,
          "1117" -> TaxYearFormatError,
          "1244" -> PropertyIdFormatError,
          "1216" -> InternalError,
          "5010" -> NotFoundError,
          "5000" -> RuleTaxYearNotSupportedError
        )

        errorMap.foreach(args => (serviceError).tupled(args))
      }
    }

    trait Test extends MockRetrieveForeignPropertyDetailsConnector {
      val service = new RetrieveForeignPropertyDetailsService(mockConnector)

      val requestData: RetrieveForeignPropertyDetailsRequestData =
        Def1_RetrieveForeignPropertyDetailsRequestData(
          Nino("AA123456A"),
          BusinessId("XAIS12345678910"),
          TaxYear.fromMtd("2025-26"),
          Some(PropertyId("8e8b8450-dc1b-4360-8109-7067337b42cb"))
        )
    }
  }

}
