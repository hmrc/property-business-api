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

package v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def1_Retrieve_ForeignPropertyAdjustmentsSpec extends UnitSpec {

  private val foreignPropertyAdjustments = Def1_Retrieve_ForeignPropertyAdjustments(
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
        jsonBody.as[Def1_Retrieve_ForeignPropertyAdjustments] shouldBe foreignPropertyAdjustments
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
