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

package v4.historicFhlUkPropertyPeriodSummary.list.def1

import shared.controllers.validators.Validator
import shared.models.domain.Nino
import shared.models.errors._
import shared.utils.UnitSpec
import v4.historicFhlUkPropertyPeriodSummary.list.model.request.{
  Def1_ListHistoricFhlUkPropertyPeriodSummariesRequestData,
  ListHistoricFhlUkPropertyPeriodSummariesRequestData
}

class Def1_ListHistoricFhlUkPropertyPeriodSummariesValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "X-123"
  private val validNino                      = "AA123456A"

  private val parsedNino = Nino(validNino)

  private def validator(nino: String): Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
    new Def1_ListHistoricFhlUkPropertyPeriodSummariesValidator(nino)

  "validate()" should {
    "return the parsed domain object" when {
      "given a valid nino" in {
        val result: Either[ErrorWrapper, ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
          validator(validNino).validateAndWrapResult()

        result shouldBe Right(Def1_ListHistoricFhlUkPropertyPeriodSummariesRequestData(parsedNino))
      }

    }
    "return a single error" when {
      "given an invalid nino" in {
        val result: Either[ErrorWrapper, ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
          validator("invalid").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }

}
