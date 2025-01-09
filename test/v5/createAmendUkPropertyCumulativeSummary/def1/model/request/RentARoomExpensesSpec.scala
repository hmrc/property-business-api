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

class RentARoomExpensesSpec extends UnitSpec {

  val rentARoomJson: JsValue = Json.parse(
    """
      |{
      | "amountClaimed": 947.66
      |}
        """.stripMargin
  )

  val rentARoomExpenses: RentARoomExpenses = RentARoomExpenses(
    amountClaimed = Some(947.66)
  )

  "RentARoomExpenses" should {
    "read from json" in {
      rentARoomJson.as[RentARoomExpenses] shouldBe rentARoomExpenses
    }

    "write from json" in {
      Json.toJson(rentARoomExpenses) shouldBe rentARoomJson
    }
  }

}
