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

package v3.models.request.createAmendForeignPropertyAnnualSubmission

import play.api.libs.json.Json
import support.UnitSpec

class CreateAmendForeignPropertyAnnualSubmissionRequestBodySpec extends UnitSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val fhlModel =
    CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)

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

  private val nonFhlModel =
    CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = None, foreignNonFhlProperty = Some(List(foreignNonFhlEntry)))

  private val nonFhlMtdJson = Json.parse(s"""
      |{
      |   "foreignNonFhlProperty":[ $foreignNonFhlEntryMtdJson ]
      |}
      |""".stripMargin)

  private val nonFhlDownstreamJson = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignNonFhlEntryDownstreamJson ]
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson
          .as[CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe createAmendForeignPropertyAnnualSubmissionRequestBody
      }
    }
    "passed valid mtd JSON with just Fhl" should {
      "return the model" in {
        fhlMtdJson
          .as[CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe fhlModel
      }
    }
    "passed valid mtd JSON with just NonFhl" should {
      "return the model" in {
        nonFhlMtdJson
          .as[CreateAmendForeignPropertyAnnualSubmissionRequestBody] shouldBe nonFhlModel
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
        Json.toJson(nonFhlModel) shouldBe nonFhlDownstreamJson
      }
    }
  }

}
