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

package v6.createAmendForeignPropertyAnnualSubmission.def1.model.request

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBodySpec extends UnitSpec with Def1_Fixtures {

  private val fhlModel =
    Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = Some(foreignFhlEea), foreignProperty = None)

  private val fhlMtdJson = Json.parse(s"""
       |{
       |   "foreignFhlEea": $foreignFhlEeaMtdJson
       |}
       |""".stripMargin)

  private val fhlDownstreamJson = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaDownstreamJson
      |}
      |""".stripMargin)

  private val foreignPropertyModel =
    Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = None, foreignProperty = Some(List(foreignEntry)))

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
        createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson
          .as[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe createAmendForeignPropertyAnnualSubmissionRequestBody
      }
    }
    "passed valid mtd JSON with just Fhl" should {
      "return the model" in {
        fhlMtdJson
          .as[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe fhlModel
      }
    }
    "passed valid mtd JSON with just foreignProperty" should {
      "return the model" in {
        foreignPropertyMtdJson
          .as[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe foreignPropertyModel
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(
          createAmendForeignPropertyAnnualSubmissionRequestBody) shouldBe createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson
      }
    }
    "passed a model with minimal fields" should {
      "return downstream JSON" in {
        Json.toJson(fhlModel) shouldBe fhlDownstreamJson
      }
    }
    "passed a model with just Fhl" should {
      "return downstream JSON" in {
        Json.toJson(foreignPropertyModel) shouldBe foreignPropertyDownstreamJson
      }
    }
  }

}
