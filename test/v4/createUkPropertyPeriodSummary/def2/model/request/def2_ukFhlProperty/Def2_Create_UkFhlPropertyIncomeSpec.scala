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

package v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Create_UkPropertyIncomeRentARoom

class Def2_Create_UkFhlPropertyIncomeSpec extends UnitSpec {

  private val requestBody =
    Def2_Create_UkFhlPropertyIncome(Some(5000.99), Some(3123.21), Some(Def2_Create_UkPropertyIncomeRentARoom(Some(532.12))))

  private val mtdJson = Json.parse("""
    |{
    |    "periodAmount": 5000.99,
    |    "taxDeducted": 3123.21,
    |    "rentARoom": {
    |        "rentsReceived": 532.12
    |    }
    |}
    |""".stripMargin)

  private val downstreamJson = Json.parse("""
    |{
    |    "periodAmount": 5000.99,
    |    "taxDeducted": 3123.21,
    |    "ukFhlRentARoom": {
    |        "rentsReceived": 532.12
    |    }
    |}
    |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def2_Create_UkFhlPropertyIncome] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe downstreamJson
      }
    }
  }

}
