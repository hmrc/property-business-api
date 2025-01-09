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

package v5.createAmendUkPropertyCumulativeSummary.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class IncomeSpec extends UnitSpec {

  val incomeRequestJson: JsValue = Json.parse(
    """
      |{
      |  "premiumsOfLeaseGrant": 3.11,
      |  "reversePremiums": 3.12,
      |  "periodAmount": 3.13,
      |  "taxDeducted": 3.14,
      |  "otherIncome": 3.15,
      |  "rentARoom": {
      |     "rentsReceived": 3.16
      |  }
      |}
      """.stripMargin
  )

  val incomeMtdRequestJson: JsValue = Json.parse(
    """
      |{
      |  "premiumsOfLeaseGrant": 3.11,
      |  "reversePremiums": 3.12,
      |  "periodAmount": 3.13,
      |  "taxDeducted": 3.14,
      |  "otherIncome": 3.15,
      |  "ukOtherRentARoom": {
      |     "rentsReceived": 3.16
      |  }
      |}
    """.stripMargin
  )

  val rentARoomIncome: RentARoomIncome = RentARoomIncome(
    rentsReceived = Some(3.16)
  )

  val income: Income = Income(
    premiumsOfLeaseGrant = Some(3.11),
    reversePremiums = Some(3.12),
    periodAmount = Some(3.13),
    taxDeducted = Some(3.14),
    otherIncome = Some(3.15),
    rentARoom = Some(rentARoomIncome)
  )

  "Income" should {
    "read from json" in {
      incomeRequestJson.as[Income] shouldBe income
    }

    "write to json" in {
      Json.toJson(income) shouldBe incomeMtdRequestJson
    }
  }

}
