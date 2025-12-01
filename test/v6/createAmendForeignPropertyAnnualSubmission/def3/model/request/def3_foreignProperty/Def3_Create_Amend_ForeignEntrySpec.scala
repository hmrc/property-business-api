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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.Def3_Fixtures

class Def3_Create_Amend_ForeignEntrySpec extends UnitSpec with Def3_Fixtures {

  private val allowancesOnly = Def3_Create_Amend_ForeignEntry(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    allowances = Some(def3_foreignAllowances),
    adjustments = None
  )

  private val allowancesOnlyMtdJson = Json.parse(s"""
     |{
     |   "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
     |   "allowances": $def3_foreignAllowancesMtdJson
     |}
     |""".stripMargin)

  private val allowancesOnlyDownstreamJson = Json.parse(s"""
    |{
    |   "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
    |   "allowances": $def3_foreignAllowancesDownstreamJson
    |}
    |""".stripMargin)

  private val adjustmentsOnly = Def3_Create_Amend_ForeignEntry(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    allowances = None,
    adjustments = Some(def3_foreignAdjustments)
  )

  private val adjustmentsOnlyMtdJson = Json.parse(s"""
    |{
    |   "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
    |   "adjustments": $def3_foreignAdjustmentsMtdJson
    |}""".stripMargin)

  private val adjustmentsOnlyDownstreamJson = Json.parse(s"""
   |{
   |   "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
   |   "adjustments": $def3_foreignAdjustmentsDownstreamJson
   |}
   |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        def3_foreignEntryMtdJson.as[Def3_Create_Amend_ForeignEntry] shouldBe def3_foreignEntry
      }
    }
    "passed valid mtd JSON with allowances only" should {
      "return the model" in {
        allowancesOnlyMtdJson.as[Def3_Create_Amend_ForeignEntry] shouldBe allowancesOnly
      }
    }
    "passed valid mtd JSON with adjustments only" should {
      "return the model" in {
        adjustmentsOnlyMtdJson.as[Def3_Create_Amend_ForeignEntry] shouldBe adjustmentsOnly
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(def3_foreignEntry) shouldBe def3_foreignEntryDownstreamJson
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
