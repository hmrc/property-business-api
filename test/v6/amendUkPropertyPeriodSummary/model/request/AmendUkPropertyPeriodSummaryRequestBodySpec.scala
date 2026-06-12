/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.amendUkPropertyPeriodSummary.model.request

import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json}
import v6.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.*
import v6.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty.*
import v6.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty.*
import v6.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.*

class AmendUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec {

  "AmendUkPropertyPeriodSummaryRequestBody" when {
    "sealed trait" should {
      "be extended by all request body variants" in {
        // Test that all variants extend the sealed trait
        val def1: AmendUkPropertyPeriodSummaryRequestBody           = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, None)
        val def2: AmendUkPropertyPeriodSummaryRequestBody           = Def2_AmendUkPropertyPeriodSummaryRequestBody(None, None)
        val def2Submission: AmendUkPropertyPeriodSummaryRequestBody = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(None, None)

        def1 shouldBe a[AmendUkPropertyPeriodSummaryRequestBody]
        def2 shouldBe a[AmendUkPropertyPeriodSummaryRequestBody]
        def2Submission shouldBe a[AmendUkPropertyPeriodSummaryRequestBody]
      }
    }
  }

  "Def1_AmendUkPropertyPeriodSummaryRequestBody" when {
    val def1FhlProperty = Def1_Amend_UkFhlProperty(
      Some(Def1_Amend_UkFhlPropertyIncome(Some(1000.0), Some(100.0), None)),
      Some(Def1_Amend_UkFhlPropertyExpenses(Some(200.0), Some(50.0), None, None, None, None, None, None, None))
    )

    val def1NonFhlProperty = Def1_Amend_UkNonFhlProperty(
      Some(Def1_Amend_UkNonFhlPropertyIncome(Some(2000.0), None, Some(1500.0), Some(200.0), None, None)),
      Some(Def1_Amend_UkNonFhlPropertyExpenses(Some(300.0), Some(100.0), None, None, None, None, None, None, None, None, None))
    )

    "created with both properties" should {
      "construct successfully" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(Some(def1FhlProperty), Some(def1NonFhlProperty))
        body.ukFhlProperty shouldBe Some(def1FhlProperty)
        body.ukNonFhlProperty shouldBe Some(def1NonFhlProperty)
      }
    }

    "created with only FHL property" should {
      "construct successfully" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(Some(def1FhlProperty), None)
        body.ukFhlProperty shouldBe Some(def1FhlProperty)
        body.ukNonFhlProperty shouldBe None
      }
    }

    "created with only non-FHL property" should {
      "construct successfully" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, Some(def1NonFhlProperty))
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe Some(def1NonFhlProperty)
      }
    }

    "created with no properties" should {
      "construct successfully" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, None)
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe None
      }
    }

    "serialized to JSON" should {
      "produce correct JSON with both properties" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(Some(def1FhlProperty), Some(def1NonFhlProperty))
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe true
        (json \ "ukOtherProperty").isDefined shouldBe true
      }

      "produce correct JSON with only FHL property" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(Some(def1FhlProperty), None)
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe true
        (json \ "ukOtherProperty").isDefined shouldBe false
      }

      "produce correct JSON with only non-FHL property" in {
        val body = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, Some(def1NonFhlProperty))
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe false
        (json \ "ukOtherProperty").isDefined shouldBe true
      }
    }

  }

  "Def2_AmendUkPropertyPeriodSummaryRequestBody" when {
    val def2FhlProperty = Def2_Amend_UkFhlProperty(
      Some(Def2_Amend_UkFhlPropertyIncome(Some(1000.0), Some(100.0), None)),
      Some(Def2_Amend_UkFhlPropertyExpenses(Some(200.0), Some(50.0), None, None, None, None, None, None, None))
    )

    val def2NonFhlProperty = Def2_Amend_UkNonFhlProperty(
      Some(Def2_Amend_UkNonFhlPropertyIncome(Some(2000.0), None, Some(1500.0), Some(200.0), None, None)),
      Some(Def2_Amend_UkNonFhlPropertyExpenses(Some(300.0), Some(100.0), None, None, None, None, None, None, None, None, None))
    )

    "created with both properties" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummaryRequestBody(Some(def2FhlProperty), Some(def2NonFhlProperty))
        body.ukFhlProperty shouldBe Some(def2FhlProperty)
        body.ukNonFhlProperty shouldBe Some(def2NonFhlProperty)
      }
    }

    "created with only FHL property" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummaryRequestBody(Some(def2FhlProperty), None)
        body.ukFhlProperty shouldBe Some(def2FhlProperty)
        body.ukNonFhlProperty shouldBe None
      }
    }

    "created with only non-FHL property" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummaryRequestBody(None, Some(def2NonFhlProperty))
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe Some(def2NonFhlProperty)
      }
    }

    "created with no properties" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummaryRequestBody(None, None)
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe None
      }
    }

    "serialized to JSON" should {
      "produce correct JSON with both properties" in {
        val body = Def2_AmendUkPropertyPeriodSummaryRequestBody(Some(def2FhlProperty), Some(def2NonFhlProperty))
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe true
        (json \ "ukOtherProperty").isDefined shouldBe true
      }
    }

  }

  "Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody" when {
    val def2FhlProperty = Def2_Amend_UkFhlProperty(
      Some(Def2_Amend_UkFhlPropertyIncome(Some(1000.0), Some(100.0), None)),
      Some(Def2_Amend_UkFhlPropertyExpenses(Some(200.0), Some(50.0), None, None, None, None, None, None, None))
    )

    val def2NonFhlPropertySubmission = Def2_Amend_UkNonFhlPropertySubmission(
      Some(Def2_Amend_UkNonFhlPropertyIncome(Some(2000.0), None, Some(1500.0), Some(200.0), None, None)),
      Some(Def2_Amend_UkNonFhlPropertyExpensesSubmission(Some(300.0), Some(100.0), None, None, None, None, None, None, None, None, None, None, None))
    )

    "created with both properties" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(Some(def2FhlProperty), Some(def2NonFhlPropertySubmission))
        body.ukFhlProperty shouldBe Some(def2FhlProperty)
        body.ukNonFhlProperty shouldBe Some(def2NonFhlPropertySubmission)
      }
    }

    "created with only FHL property" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(Some(def2FhlProperty), None)
        body.ukFhlProperty shouldBe Some(def2FhlProperty)
        body.ukNonFhlProperty shouldBe None
      }
    }

    "created with only non-FHL property" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(None, Some(def2NonFhlPropertySubmission))
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe Some(def2NonFhlPropertySubmission)
      }
    }

    "created with no properties" should {
      "construct successfully" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(None, None)
        body.ukFhlProperty shouldBe None
        body.ukNonFhlProperty shouldBe None
      }
    }

    "serialized to JSON" should {
      "produce correct JSON with both properties" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(Some(def2FhlProperty), Some(def2NonFhlPropertySubmission))
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe true
        (json \ "ukOtherProperty").isDefined shouldBe true
      }

      "produce correct JSON with only FHL property" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(Some(def2FhlProperty), None)
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe true
        (json \ "ukOtherProperty").isDefined shouldBe false
      }

      "produce correct JSON with only non-FHL property" in {
        val body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(None, Some(def2NonFhlPropertySubmission))
        val json = Json.toJson(body)

        (json \ "ukFhlProperty").isDefined shouldBe false
        (json \ "ukOtherProperty").isDefined shouldBe true
      }
    }

    "round-tripped through JSON" should {
      "maintain equality" in {
        val original     = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(Some(def2FhlProperty), None)
        val json         = Json.toJson(original)
        val deserialized = json.as[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody]
        deserialized shouldBe original
      }
    }
  }

  "JSON serialization/deserialization" when {
    "handling edge cases" should {
      "handle empty JSON objects" in {
        val json                 = Json.parse("{}")
        val def1Result           = json.as[Def1_AmendUkPropertyPeriodSummaryRequestBody]
        val def2Result           = json.as[Def2_AmendUkPropertyPeriodSummaryRequestBody]
        val def2SubmissionResult = json.as[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody]

        def1Result.ukFhlProperty shouldBe None
        def1Result.ukNonFhlProperty shouldBe None
        def2Result.ukFhlProperty shouldBe None
        def2Result.ukNonFhlProperty shouldBe None
        def2SubmissionResult.ukFhlProperty shouldBe None
        def2SubmissionResult.ukNonFhlProperty shouldBe None
      }

      "handle null values in JSON" in {
        val json                 = Json.parse("""{"ukFhlProperty": null, "ukOtherProperty": null}""")
        val def1Result           = json.as[Def1_AmendUkPropertyPeriodSummaryRequestBody]
        val def2Result           = json.as[Def2_AmendUkPropertyPeriodSummaryRequestBody]
        val def2SubmissionResult = json.as[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody]

        def1Result.ukFhlProperty shouldBe None
        def1Result.ukNonFhlProperty shouldBe None
        def2Result.ukFhlProperty shouldBe None
        def2Result.ukNonFhlProperty shouldBe None
        def2SubmissionResult.ukFhlProperty shouldBe None
        def2SubmissionResult.ukNonFhlProperty shouldBe None
      }
    }
  }

}
