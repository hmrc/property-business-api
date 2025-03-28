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

package v6.historicNonFhlUkPropertyPeriodSummary.amend.model.response

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class AmendHistoricNonFhlUkPropertyPeriodSummaryResponseSpec extends UnitSpec {

  val response: AmendHistoricNonFhlUkPropertyPeriodSummaryResponse = AmendHistoricNonFhlUkPropertyPeriodSummaryResponse(
    transactionReference = "2017090920170909")

  val downstreamJson: JsValue = Json.parse("""
      |{
      |    "transactionReference": "2017090920170909"
      |}
      |""".stripMargin)

  "reads" when {
    "given a valid JSON object" should {
      "return the parsed object" in {
        downstreamJson.as[AmendHistoricNonFhlUkPropertyPeriodSummaryResponse] shouldBe response
      }
    }
  }

}
