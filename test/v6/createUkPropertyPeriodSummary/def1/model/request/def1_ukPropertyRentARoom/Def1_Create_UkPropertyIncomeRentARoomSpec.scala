/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.createUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom

import play.api.libs.json.Json
import api.utils.UnitSpec

class Def1_Create_UkPropertyIncomeRentARoomSpec extends UnitSpec {

  "Def1_Create_UkPropertyIncomeRentARoom" when {
    "created with rentsReceived" should {
      "construct successfully" in {
        val income = Def1_Create_UkPropertyIncomeRentARoom(Some(1234.56))
        income.rentsReceived shouldBe Some(1234.56)
      }
    }

    "created with no rentsReceived" should {
      "construct successfully" in {
        val income = Def1_Create_UkPropertyIncomeRentARoom(None)
        income.rentsReceived shouldBe None
      }
    }

    "serialized to JSON" should {
      "produce correct JSON with rentsReceived" in {
        val income = Def1_Create_UkPropertyIncomeRentARoom(Some(1234.56))
        val json   = Json.toJson(income)

        (json \ "rentsReceived").as[BigDecimal] shouldBe 1234.56
      }

      "produce correct JSON with no rentsReceived" in {
        val income = Def1_Create_UkPropertyIncomeRentARoom(None)
        val json   = Json.toJson(income)

        (json \ "rentsReceived").isDefined shouldBe false
      }
    }

    "deserialized from JSON" should {
      "reconstruct object correctly with rentsReceived" in {
        val json = Json.parse("""
          {
            "rentsReceived": 1234.56
          }
        """)

        val result = json.as[Def1_Create_UkPropertyIncomeRentARoom]
        result.rentsReceived shouldBe Some(1234.56)
      }

      "reconstruct object correctly with no rentsReceived" in {
        val json = Json.parse("{}")

        val result = json.as[Def1_Create_UkPropertyIncomeRentARoom]
        result.rentsReceived shouldBe None
      }
    }

    "round-tripped through JSON" should {
      "maintain equality with rentsReceived" in {
        val original     = Def1_Create_UkPropertyIncomeRentARoom(Some(1234.56))
        val json         = Json.toJson(original)
        val deserialized = json.as[Def1_Create_UkPropertyIncomeRentARoom]
        deserialized shouldBe original
      }

      "maintain equality with no rentsReceived" in {
        val original     = Def1_Create_UkPropertyIncomeRentARoom(None)
        val json         = Json.toJson(original)
        val deserialized = json.as[Def1_Create_UkPropertyIncomeRentARoom]
        deserialized shouldBe original
      }
    }
  }

  "JSON serialization/deserialization" when {
    "handling edge cases" should {
      "handle empty JSON objects" in {
        val json   = Json.parse("{}")
        val result = json.as[Def1_Create_UkPropertyIncomeRentARoom]

        result.rentsReceived shouldBe None
      }

      "handle null values in JSON" in {
        val json   = Json.parse("""{"rentsReceived": null}""")
        val result = json.as[Def1_Create_UkPropertyIncomeRentARoom]

        result.rentsReceived shouldBe None
      }
    }
  }

}
