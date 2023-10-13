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

package v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl

import play.api.libs.json.Json
import api.support.UnitSpec
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionFixture

class ForeignNonFhlEntrySpec extends UnitSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val allowancesOnly = ForeignNonFhlEntry(
    countryCode = "GER",
    allowances = Some(foreignNonFhlAllowances),
    adjustments = None
  )

  private val allowancesOnlyMtdJson = Json.parse(s"""
     |{
     |   "countryCode": "GER",
     |   "allowances": $foreignNonFhlAllowancesMtdJson
     |}
     |""".stripMargin)

  private val allowancesOnlyDownstreamJson = Json.parse(s"""
    |{
    |   "countryCode": "GER",
    |   "allowances": $foreignNonFhlAllowancesDownstreamJson
    |}
    |""".stripMargin)

  private val adjustmentsOnly = ForeignNonFhlEntry(
    countryCode = "GER",
    allowances = None,
    adjustments = Some(foreignNonFhlAdjustments)
  )

  private val adjustmentsOnlyMtdJson = Json.parse(s"""
    |{
    |   "countryCode": "GER",
    |   "adjustments": $foreignNonFhlAdjustmentsMtdJson
    |}""".stripMargin)

  private val adjustmentsOnlyDownstreamJson = Json.parse(s"""
   |{
   |   "countryCode": "GER",
   |   "adjustments": $foreignNonFhlAdjustmentsDownstreamJson
   |}
   |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignNonFhlEntryMtdJson.as[ForeignNonFhlEntry] shouldBe foreignNonFhlEntry
      }
    }
    "passed valid mtd JSON with allowances only" should {
      "return the model" in {
        allowancesOnlyMtdJson.as[ForeignNonFhlEntry] shouldBe allowancesOnly
      }
    }
    "passed valid mtd JSON with adjustments only" should {
      "return the model" in {
        adjustmentsOnlyMtdJson.as[ForeignNonFhlEntry] shouldBe adjustmentsOnly
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignNonFhlEntry) shouldBe foreignNonFhlEntryDownstreamJson
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
