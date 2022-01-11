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

package v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertyAdjustmentsSpec extends UnitSpec with JsonErrorValidators {

  private val foreignPropertyAdjustments = ForeignPropertyAdjustments(
    Some(100.25),
    Some(100.25)
  )

  private val jsonBody = Json.parse(
    """
      |{
      |    "privateUseAdjustment":100.25,
      |    "balancingCharge":100.25
      |}
    """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[ForeignPropertyAdjustments] shouldBe foreignPropertyAdjustments
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignPropertyAdjustments) shouldBe jsonBody
      }
    }
  }
}
