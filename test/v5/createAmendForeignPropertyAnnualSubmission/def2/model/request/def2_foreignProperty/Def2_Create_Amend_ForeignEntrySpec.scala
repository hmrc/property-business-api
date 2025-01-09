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

package v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.Def2_Fixtures

class Def2_Create_Amend_ForeignEntrySpec extends UnitSpec with Def2_Fixtures {

  private val allowancesOnly = Def2_Create_Amend_ForeignEntry(
    countryCode = "GER",
    allowances = Some(foreignAllowances),
    adjustments = None
  )

  private val allowancesOnlyMtdJson = Json.parse(s"""
     |{
     |   "countryCode": "GER",
     |   "allowances": $foreignAllowancesMtdJson
     |}
     |""".stripMargin)

  private val allowancesOnlyDownstreamJson = Json.parse(s"""
    |{
    |   "countryCode": "GER",
    |   "allowances": $foreignAllowancesDownstreamJson
    |}
    |""".stripMargin)

  private val adjustmentsOnly = Def2_Create_Amend_ForeignEntry(
    countryCode = "GER",
    allowances = None,
    adjustments = Some(foreignAdjustments)
  )

  private val adjustmentsOnlyMtdJson = Json.parse(s"""
    |{
    |   "countryCode": "GER",
    |   "adjustments": $foreignAdjustmentsMtdJson
    |}""".stripMargin)

  private val adjustmentsOnlyDownstreamJson = Json.parse(s"""
   |{
   |   "countryCode": "GER",
   |   "adjustments": $foreignAdjustmentsDownstreamJson
   |}
   |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignEntryMtdJson.as[Def2_Create_Amend_ForeignEntry] shouldBe foreignEntry
      }
    }
    "passed valid mtd JSON with allowances only" should {
      "return the model" in {
        allowancesOnlyMtdJson.as[Def2_Create_Amend_ForeignEntry] shouldBe allowancesOnly
      }
    }
    "passed valid mtd JSON with adjustments only" should {
      "return the model" in {
        adjustmentsOnlyMtdJson.as[Def2_Create_Amend_ForeignEntry] shouldBe adjustmentsOnly
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignEntry) shouldBe foreignEntryDownstreamJson
      }
    }
    "passed a model with allowances only" should {
      "return downstream JSON" in {
        Json.toJson(allowancesOnly) shouldBe allowancesOnlyDownstreamJson
      }
    }
    "passed a model with adjustments only" should {
      "return downstream JSON" in {
        Json.toJson(adjustmentsOnly) shouldBe adjustmentsOnlyDownstreamJson
      }
    }
  }

}
