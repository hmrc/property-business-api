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

package v4.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignFhlEea

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def2_Create_ForeignFhlEeaIncomeSpec extends UnitSpec {

  private val mtdJson = Json.parse(
    """
      |{
      |  "rentAmount": 567.83
      |}
    """.stripMargin
  )

  private val model = Def2_Create_ForeignFhlEeaIncome(rentAmount = Some(567.83))

  private val downstreamJson = Json.parse(
    """
      |{
      |  "rentAmount": 567.83
      |}
    """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[Def2_Create_ForeignFhlEeaIncome] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe downstreamJson
      }
    }
  }

}
