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

package v5.createAmendForeignPropertyAnnualSubmission.def2.model.request

import play.api.libs.json.Json
import support.UnitSpec

class Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBodySpec extends UnitSpec with Def2_Fixtures {

  private val foreignPropertyModel =
    Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody( foreignProperty = Some(List(foreignEntry)))

  private val foreignPropertyMtdJson = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignEntryMtdJson ]
      |}
      |""".stripMargin)

  private val foreignPropertyDownstreamJson = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignEntryDownstreamJson ]
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        def2_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson
          .as[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe def2_createAmendForeignPropertyAnnualSubmissionRequestBody
      }
    }

    "passed valid mtd JSON with just foreignProperty" should {
      "return the model" in {
        foreignPropertyMtdJson
          .as[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe foreignPropertyModel
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(
          def2_createAmendForeignPropertyAnnualSubmissionRequestBody) shouldBe def2_createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson
      }
    }

    "passed a model with just foreignProperty" should {
      "return downstream JSON" in {
        Json.toJson(foreignPropertyModel) shouldBe foreignPropertyDownstreamJson
      }
    }
  }

}
