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

package v3.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v3.models.utils.JsonErrorValidators

class RentARoomSpec extends UnitSpec with JsonErrorValidators {

  val rentARoomIncome = RentARoomIncome(Some(5000.99))

  val rentARoomExpenses = RentARoomExpenses(Some(5000.99))

  val writesIncomeJson: JsValue = Json.parse(
    """{
      |      "rentsReceived":5000.99
      |    }
      |""".stripMargin
  )

  val readsIncomeJson: JsValue = Json.parse(
    """{
      |      "rentsReceived":5000.99
      |    }
      |""".stripMargin
  )

  val writesExpensesJson: JsValue = Json.parse(
    """{
      |      "amountClaimed":5000.99
      |    }
      |""".stripMargin
  )

  val readsExpensesJson: JsValue = Json.parse(
    """{
      |      "amountClaimed":5000.99
      |    }
      |""".stripMargin
  )

  "reads" when {
    "passed a valid Income JSON" should {
      "return a valid object" in {
        readsIncomeJson.as[RentARoomIncome] shouldBe rentARoomIncome
      }
    }
    "passed a valid Expenses JSON" should {
      "return a valid object" in {
        readsExpensesJson.as[RentARoomExpenses] shouldBe rentARoomExpenses
      }
    }
  }

  "writes" when {
    "passed valid Income object" should {
      "return valid JSON" in {
        Json.toJson(rentARoomIncome) shouldBe writesIncomeJson
      }
    }
    "passed a valid Expenses object" should {
      "return valid JSON" in {
        Json.toJson(rentARoomExpenses) shouldBe writesExpensesJson
      }
    }
  }

}
