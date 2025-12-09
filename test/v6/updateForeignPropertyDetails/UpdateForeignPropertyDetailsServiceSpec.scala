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

package v6.updateForeignPropertyDetails

import common.models.domain.PropertyId
import common.models.errors.*
import shared.models.domain.*
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.updateForeignPropertyDetails.def1.model.Def1_UpdateForeignPropertyDetailsFixtures
import v6.updateForeignPropertyDetails.def1.model.Def1_UpdateForeignPropertyDetailsFixtures.def1_UpdateForeignPropertyDetailsModel
import v6.updateForeignPropertyDetails.def1.model.request.Def1_UpdateForeignPropertyDetailsRequestData

import scala.concurrent.Future

class UpdateForeignPropertyDetailsServiceSpec extends ServiceSpec with MockUpdateForeignPropertyDetailsConnector {

  implicit override val correlationId: String = "X-123"

  private val nino       = Nino("AA999999A")
  private val propertyId = PropertyId("8e8b8450-dc1b-4360-8109-7067337b42cb")
  private val taxYear    = TaxYear.fromMtd("2025-26")

  private val requestData = Def1_UpdateForeignPropertyDetailsRequestData(nino, propertyId, taxYear, def1_UpdateForeignPropertyDetailsModel)

  private val service = new UpdateForeignPropertyDetailsService(mockConnector)

  "UpdateForeignPropertyDetailsService" should {
    "be successful" when {
      "given a valid request" in {
        MockedUpdateForeignPropertyDetailsConnector
          .updateForeignPropertyDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result = await(service.updateForeignPropertyDetails(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "be unsuccessful and map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in {

          MockedUpdateForeignPropertyDetailsConnector
            .updateForeignPropertyDetails(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result = await(service.updateForeignPropertyDetails(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "1215" -> NinoFormatError,
        "1244" -> PropertyIdFormatError,
        "1117" -> TaxYearFormatError,
        "1000" -> InternalError,
        "1216" -> InternalError,
        "5010" -> NotFoundError,
        "1245" -> RuleDuplicatePropertyNameError,
        "1246" -> RuleTaxYearBeforeBusinessStartError,
        "1247" -> RuleEndDateAfterTaxYearEndError,
        "1248" -> RulePropertyBusinessCeasedError,
        "1249" -> RuleMissingEndDetailsError,
        "4200" -> RuleOutsideAmendmentWindowError,
        "5000" -> RuleTaxYearNotSupportedError
      )

      errors.foreach(serviceError.tupled)
    }
  }

}
