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

package v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignFhlEea

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_Fixtures

class Def1_Create_Amend_ForeignFhlEeaSpec extends UnitSpec with Def1_Fixtures {

  private val allowancesOnly = Def1_Create_Amend_ForeignFhlEea(
    allowances = Some(foreignFhlEeaAllowances),
    adjustments = None
  )

  private val allowancesOnlyMtdJson = Json.parse(s"""
      |{
      |   "allowances": $foreignFhlEeaAllowancesMtdJson
      |}""".stripMargin)

  private val allowancesOnlyDownstreamJson = Json.parse(s"""
       |{
       |   "allowances": $foreignFhlEeaAllowancesDownstreamJson
       |}""".stripMargin)

  private val adjustmentsOnly = Def1_Create_Amend_ForeignFhlEea(
    allowances = None,
    adjustments = Some(foreignFhlEeaAdjustments)
  )

  private val adjustmentsOnlyMtdJson = Json.parse(s"""
       |{
       |   "adjustments": $foreignFhlEeaAdjustmentsMtdJson
       |}""".stripMargin)

  private val adjustmentsOnlyDownstreamJson = Json.parse(s"""
       |{
       |   "adjustments": $foreignFhlEeaAdjustmentsDownstreamJson
       |}""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignFhlEeaMtdJson.as[Def1_Create_Amend_ForeignFhlEea] shouldBe foreignFhlEea
      }
    }
    "passed valid mtd JSON with allowances only" should {
      "return the model" in {
        allowancesOnlyMtdJson.as[Def1_Create_Amend_ForeignFhlEea] shouldBe allowancesOnly
      }
    }
    "passed valid mtd JSON with adjustments only" should {
      "return the model" in {
        adjustmentsOnlyMtdJson.as[Def1_Create_Amend_ForeignFhlEea] shouldBe adjustmentsOnly
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignFhlEea) shouldBe foreignFhlEeaDownstreamJson
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
