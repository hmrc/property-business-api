/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.request.amendForeignPropertyAnnualSubmission

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class AmendForeignPropertyAnnualSubmissionRequestBodySpec extends UnitSpec with JsonErrorValidators with AmendForeignPropertyAnnualSubmissionFixture {

  private val fhlModel: AmendForeignPropertyAnnualSubmissionRequestBody =
    AmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)

  private val fhlMtdJson: JsValue = Json.parse(s"""
       |{
       |   "foreignFhlEea": $foreignFhlEeaMtdJson
       |}
       |""".stripMargin)

  private val fhlDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaDownstreamJson
      |}
      |""".stripMargin)

  private val nonFhlModel: AmendForeignPropertyAnnualSubmissionRequestBody =
    AmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea = None, foreignNonFhlProperty = Some(Seq(foreignNonFhlEntry)))

  private val nonFhlMtdJson: JsValue        = Json.parse(s"""
      |{
      |   "foreignNonFhlProperty":[ $foreignNonFhlEntryMtdJson ]
      |}
      |""".stripMargin)

  private val nonFhlDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignNonFhlEntryDownstreamJson ]
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        amendForeignPropertyAnnualSubmissionRequestBodyMtdJson
          .as[AmendForeignPropertyAnnualSubmissionRequestBody] shouldBe amendForeignPropertyAnnualSubmissionRequestBody
      }
    }
    "passed valid mtd JSON with just Fhl" should {
      "return the model" in {
        fhlMtdJson
          .as[AmendForeignPropertyAnnualSubmissionRequestBody] shouldBe fhlModel
      }
    }
    "passed valid mtd JSON with just NonFhl" should {
      "return the model" in {
        nonFhlMtdJson
          .as[AmendForeignPropertyAnnualSubmissionRequestBody] shouldBe nonFhlModel
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(amendForeignPropertyAnnualSubmissionRequestBody) shouldBe amendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson
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
