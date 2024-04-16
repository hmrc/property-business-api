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

package v4.controllers.amendForeignPropertyPeriodSummary.def2.model.request.foreignPropertyEntry

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.controllers.amendForeignPropertyPeriodSummary.def2.model.request.def2_foreignPropertyEntry.Def2_ForeignNonFhlPropertyRentIncome

class Def2_ForeignNonFhlPropertyRentIncomeSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "rentAmount": 34456.30
      |}
    """.stripMargin
  )

  val model: Def2_ForeignNonFhlPropertyRentIncome = Def2_ForeignNonFhlPropertyRentIncome(rentAmount = Some(34456.30))

  val ifsJson: JsValue = Json.parse(
    """
      |{
      |  "rentAmount": 34456.30
      |}
    """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[Def2_ForeignNonFhlPropertyRentIncome] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe ifsJson
      }
    }
  }

}
