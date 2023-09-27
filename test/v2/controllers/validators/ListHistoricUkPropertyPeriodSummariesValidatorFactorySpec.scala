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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.models.domain.Nino
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData

class ListHistoricUkPropertyPeriodSummariesValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "X-123"
  private val validNino                      = "AA123456A"

  private val parsedNino = Nino(validNino)

  private val validatorFactory = new ListHistoricUkPropertyPeriodSummariesValidatorFactory()

  private def validator(nino: String): Validator[ListHistoricUkPropertyPeriodSummariesRequestData] = validatorFactory.validator(nino)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid nino is supplied" in {
        val result: Either[ErrorWrapper, ListHistoricUkPropertyPeriodSummariesRequestData] =
          validator(validNino).validateAndWrapResult()

        result shouldBe Right(ListHistoricUkPropertyPeriodSummariesRequestData(parsedNino))
      }

    }
    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, ListHistoricUkPropertyPeriodSummariesRequestData] =
          validator("invalid").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }

}
