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

package v4.historicNonFhlUkPropertyPeriodSummary.amend.def1.model.request

import play.api.libs.json.Json
import shared.utils.UnitSpec

class UkNonFhlPropertyExpensesSpec extends UnitSpec with Def1_Fixtures {

  "reads" when {
    "given a valid JSON document with full data" should {
      "return a valid model" in {
        mtdJsonExpensesFull.as[UkNonFhlPropertyExpenses] shouldBe ukNonFhlPropertyExpensesFull
      }
    }

    "given a valid JSON document with consolidated data" should {
      "return a valid model" in {
        mtdJsonExpensesConsolidated.as[UkNonFhlPropertyExpenses] shouldBe ukNonFhlPropertyExpensesConsolidated
      }
    }
  }

  "writes" when {
    "given a valid object with full data" should {
      "return valid JSON" in {
        Json.toJson(ukNonFhlPropertyExpensesFull) shouldBe downstreamJsonExpensesFull
      }
    }

    "given valid object with consolidated data" should {
      "return valid JSON" in {
        Json.toJson(ukNonFhlPropertyExpensesConsolidated) shouldBe downstreamJsonExpensesConsolidated
      }
    }
  }

}
