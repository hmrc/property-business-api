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

package v6.retrieveForeignPropertyCumulativeSummary.def2.model.response

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.Def2_RetrieveForeignPropertyCumulativeSummaryFixture.*

class ExpensesSpec extends UnitSpec {

  private def extractExpensesJson(json: JsValue): JsValue = ((json \ "foreignProperty")(0) \ "expenses").get

  "Expenses" which {
    Seq(
      ("consolidated", consolidatedDownstreamJson, consolidatedMtdJson, expensesConsolidated),
      ("not consolidated", fullDownstreamJson, fullMtdJson, expenses)
    ).foreach { case (scenario, downstreamJson, mtdJson, expenses) =>
      s"is $scenario" when {
        "read from valid JSON" should {
          "return the parsed object" in {
            extractExpensesJson(downstreamJson).as[Expenses] shouldBe expenses
          }
        }

        "written to JSON" should {
          "return the expected JSON" in {
            Json.toJson(expenses) shouldBe extractExpensesJson(mtdJson)
          }
        }
      }
    }
  }

}
