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

package v6.createForeignPropertyDetails

import common.models.errors.*
import shared.models.domain.*
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.createForeignPropertyDetails.def1.model.Def1_CreateForeignPropertyDetailsFixtures
import v6.createForeignPropertyDetails.def1.model.request.Def1_CreateForeignPropertyDetailsRequestData
import v6.createForeignPropertyDetails.def1.model.response.Def1_CreateForeignPropertyDetailsResponse

import scala.concurrent.Future

class CreateForeignPropertyDetailsServiceSpec
    extends ServiceSpec
    with MockCreateForeignPropertyDetailsConnector
    with Def1_CreateForeignPropertyDetailsFixtures {

  implicit override val correlationId: String = "X-123"

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2025-26")

  private val requestData = Def1_CreateForeignPropertyDetailsRequestData(nino, businessId, taxYear, def1_CreateForeignPropertyDetailsModel)

  private val responseData: Def1_CreateForeignPropertyDetailsResponse = def1_CreateForeignPropertyDetailsResponseModel

  private val service = new CreateForeignPropertyDetailsService(mockCreateForeignPropertyDetailsConnector)

  "service" should {
    "be successful" when {
      "given a valid request" in {
        MockedCreateForeignPropertyDetailsConnector
          .createForeignPropertyDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        val result = await(service.createForeignPropertyDetails(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))
      }
    }

    "be unsuccessful and map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in {

          MockedCreateForeignPropertyDetailsConnector
            .createForeignPropertyDetails(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result = await(service.createForeignPropertyDetails(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "1215" -> NinoFormatError,
        "1007" -> BusinessIdFormatError,
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
