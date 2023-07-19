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

package v2.controllers.requestParsers.validators

import api.models.errors._
import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v2.models.request.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryRawData
import v2.models.utils.JsonErrorValidators

class AmendForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val taxYear           = "2021-22"
  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  MockAppConfig.minimumTaxV2Foreign returns 2021

  private def entryWith(countryCode: String) =
    Json.parse(s"""{
    |  "countryCode": "$countryCode",
    |  "income": {
    |    "rentIncome": {
    |      "rentAmount": 440.31
    |    },
    |    "foreignTaxCreditRelief": false,
    |    "premiumsOfLeaseGrant": 950.48,
    |    "otherPropertyIncome": 802.49,
    |    "foreignTaxPaidOrDeducted": 734.18,
    |    "specialWithholdingTaxOrForeignTaxPaid": 85.47
    |  },
    |  "expenses": {
    |    "premisesRunningCosts": 332.78,
    |    "repairsAndMaintenance": 231.45,
    |    "financialCosts": 345.23,
    |    "professionalFees": 232.45,
    |    "travelCosts": 234.67,
    |    "costOfServices": 231.56,
    |    "residentialFinancialCost": 12.34,
    |    "broughtFwdResidentialFinancialCost": 12.34,
    |    "other": 3457.9
    |  }
    |}""".stripMargin)

  private val entry = entryWith(countryCode = "AFG")

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 1123.89
       |    },
       |    "expenses": {
       |      "premisesRunningCosts": 332.78,
       |      "repairsAndMaintenance": 231.45,
       |      "financialCosts": 345.23,
       |      "professionalFees": 232.45,
       |      "travelCosts": 234.67,
       |      "costOfServices": 231.56,
       |      "other": 3457.9
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val requestBodyJson = bodyWith(entry)

  private val entryConsolidated = Json.parse("""
    |{
    |  "countryCode": "AFG",
    |  "income": {
    |    "foreignTaxCreditRelief": false
    |  },
    |  "expenses": {
    |    "consolidatedExpenses": 332.78,
    |    "residentialFinancialCost": 12.34,
    |    "broughtFwdResidentialFinancialCost": 12.34
    |  }
    |}""".stripMargin)

  private def consolidatedBodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 332.78
      |    }
      |  },
      |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = consolidatedBodyWith(entryConsolidated)

  val validator = new AmendForeignPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe Nil
      }

      "a valid consolidated expenses request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = requestBodyJsonConsolidatedExpenses)) shouldBe Nil
      }

      "a minimal fhl request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = Json.parse("""
                |{
                |  "foreignFhlEea": {
                |    "income": {
                |       "rentAmount": 567.83
                |    }
                |  }
                |}
                |""".stripMargin)
          )) shouldBe Nil
      }

      "a minimal non-fhl request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = Json.parse("""{
                |  "foreignNonFhlProperty": [
                |    {
                |      "countryCode": "AFG",
                |      "income": {
                |        "foreignTaxCreditRelief": true,
                |        "rentIncome": {
                |          "rentAmount": 440.31
                |        }
                |      }
                |    }
                |  ]
                |}
                |""".stripMargin)
          )) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = "A12344A",
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = "2020",
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a tax year that is too early is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = "2020-21",
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "a tax year range is more than 1 year" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = "2019-21",
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = "20178",
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = requestBodyJson)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return SubmissionIdFormatError error" when {
      "an invalid submissionId is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = "12345",
            body = requestBodyJson)) shouldBe
          List(SubmissionIdFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = Json.parse("""{}"""))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }

      "an object/array is empty or mandatory field is missing" when {

        Seq(
          "/foreignFhlEea",
          "/foreignFhlEea/income",
          "/foreignFhlEea/expenses"
        ).foreach(path => testWith(bodyWith(entry).replaceWithEmptyObject(path), path))

        Seq(
          (bodyWith(), "/foreignNonFhlProperty"),
          (bodyWith(entry.replaceWithEmptyObject("/income/rentIncome")), "/foreignNonFhlProperty/0/income/rentIncome"),
          (bodyWith(entry.replaceWithEmptyObject("/expenses")), "/foreignNonFhlProperty/0/expenses"),
          (bodyWith(entry.removeProperty("/countryCode")), "/foreignNonFhlProperty/0/countryCode"),
          (bodyWith(entry.removeProperty("/income/foreignTaxCreditRelief")), "/foreignNonFhlProperty/0/income/foreignTaxCreditRelief"),
          (bodyWith(entry.removeProperty("/income").removeProperty("/expenses")), "/foreignNonFhlProperty/0")
        ).foreach((testWith _).tupled)

        def testWith(body: JsValue, expectedPath: String): Unit =
          s"for $expectedPath" in {
            validator.validate(
              AmendForeignPropertyPeriodSummaryRawData(
                nino = validNino,
                businessId = validBusinessId,
                taxYear = taxYear,
                submissionId = validSubmissionId,
                body
              )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(expectedPath))))
          }
      }

      "an object is empty except for a additional (non-schema) property" in {
        val json = Json.parse("""{
                                |    "foreignFhlEea":{
                                |       "unknownField": 999.99
                                |    }
                                |}""".stripMargin)

        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = json
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignFhlEea"))))
      }

      "return ValueFormatError" when {
        val badValue = JsNumber(123.456)

        "income or (non-consolidated) expenses is invalid" when {
          Seq(
            "/foreignFhlEea/income/rentAmount",
            "/foreignFhlEea/expenses/premisesRunningCosts",
            "/foreignFhlEea/expenses/repairsAndMaintenance",
            "/foreignFhlEea/expenses/financialCosts",
            "/foreignFhlEea/expenses/professionalFees",
            "/foreignFhlEea/expenses/costOfServices",
            "/foreignFhlEea/expenses/other",
            "/foreignFhlEea/expenses/travelCosts"
          ).foreach(path => testWith(requestBodyJson.update(path, badValue), path))

          Seq(
            (bodyWith(entry.update("/income/rentIncome/rentAmount", badValue)), "/foreignNonFhlProperty/0/income/rentIncome/rentAmount"),
            (bodyWith(entry.update("/income/premiumsOfLeaseGrant", badValue)), "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant"),
            (bodyWith(entry.update("/income/otherPropertyIncome", badValue)), "/foreignNonFhlProperty/0/income/otherPropertyIncome"),
            (bodyWith(entry.update("/income/foreignTaxPaidOrDeducted", badValue)), "/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted"),
            (
              bodyWith(entry.update("/income/specialWithholdingTaxOrUkTaxPaid", badValue)),
              "/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid"),
            (bodyWith(entry.update("/expenses/premisesRunningCosts", badValue)), "/foreignNonFhlProperty/0/expenses/premisesRunningCosts"),
            (bodyWith(entry.update("/expenses/repairsAndMaintenance", badValue)), "/foreignNonFhlProperty/0/expenses/repairsAndMaintenance"),
            (bodyWith(entry.update("/expenses/financialCosts", badValue)), "/foreignNonFhlProperty/0/expenses/financialCosts"),
            (bodyWith(entry.update("/expenses/professionalFees", badValue)), "/foreignNonFhlProperty/0/expenses/professionalFees"),
            (bodyWith(entry.update("/expenses/travelCosts", badValue)), "/foreignNonFhlProperty/0/expenses/travelCosts"),
            (bodyWith(entry.update("/expenses/costOfServices", badValue)), "/foreignNonFhlProperty/0/expenses/costOfServices"),
            (bodyWith(entry.update("/expenses/residentialFinancialCost", badValue)), "/foreignNonFhlProperty/0/expenses/residentialFinancialCost"),
            (
              bodyWith(entry.update("/expenses/broughtFwdResidentialFinancialCost", badValue)),
              "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost"),
            (bodyWith(entry.update("/expenses/other", badValue)), "/foreignNonFhlProperty/0/expenses/other")
          ).foreach((testWith _).tupled)
        }

        "consolidated expenses is invalid" when {
          Seq(
            "/foreignFhlEea/expenses/consolidatedExpenses"
          ).foreach(path => testWith(requestBodyJsonConsolidatedExpenses.update(path, badValue), path))

          Seq(
            (
              consolidatedBodyWith(entryConsolidated.update("/expenses/consolidatedExpenses", badValue)),
              "/foreignNonFhlProperty/0/expenses/consolidatedExpenses"),
            (
              consolidatedBodyWith(entryConsolidated.update("/expenses/residentialFinancialCost", badValue)),
              "/foreignNonFhlProperty/0/expenses/residentialFinancialCost"),
            (
              consolidatedBodyWith(entryConsolidated.update("/expenses/broughtFwdResidentialFinancialCost", badValue)),
              "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")
          ).foreach(p => (testWith _).tupled(p))
        }

        "multiple fields are invalid" in {
          val path0 = "/foreignFhlEea/expenses/travelCosts"
          val path1 = "/foreignNonFhlProperty/0/expenses/travelCosts"
          val path2 = "/foreignNonFhlProperty/1/income/rentIncome/rentAmount"

          val json =
            bodyWith(
              entryWith(countryCode = "ZWE").update("/expenses/travelCosts", badValue),
              entry.update("/income/rentIncome/rentAmount", badValue))
              .update(path0, badValue)

          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = json
            )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path0, path1, path2))))
        }

        def testWith(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = body
            )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(expectedPath))))
        }
      }

      "return RuleCountryCodeError" when {
        "an invalid country code is provided" in {
          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = bodyWith(entryWith(countryCode = "QQQ"))
            )) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
        }
        "multiple invalid country codes are provided" in {
          validator.validate(AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = bodyWith(entryWith(countryCode = "QQQ"), entryWith(countryCode = "AAA"))
          )) shouldBe List(
            RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
        }
      }

      "return CountryCodeFormatError" when {
        "an invalid country code is provided" in {
          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = bodyWith(entryWith(countryCode = "XXXX"))
            )) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
        }
      }

      "return RuleDuplicateCountryCodeError" when {
        "a country code is duplicated" in {
          val code = "ZWE"
          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = bodyWith(entryWith(code), entryWith(code))
            )) shouldBe List(
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
        }

        "multiple country codes are duplicated" in {
          val code1 = "AFG"
          val code2 = "ZWE"
          validator.validate(AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = bodyWith(entryWith(code1), entryWith(code2), entryWith(code1), entryWith(code2))
          )) should contain theSameElementsAs List(
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code1, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code2, paths = Seq("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode"))
          )
        }
      }

      "return RuleBothExpensesSuppliedError" when {
        "consolidated and separate expenses provided for fhl" in {
          validator.validate(AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = requestBodyJson.update("foreignFhlEea/expenses/consolidatedExpenses", JsNumber(123.45))
          )) shouldBe
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/expenses"))))
        }

        "consolidated and separate expenses provided for non-fhl" in {
          validator.validate(AmendForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            submissionId = validSubmissionId,
            body = bodyWith(
              entryWith(countryCode = "ZWE").update("expenses/consolidatedExpenses", JsNumber(123.45)),
              entry.update("expenses/consolidatedExpenses", JsNumber(123.45)))
          )) shouldBe
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses", "/foreignNonFhlProperty/1/expenses"))))
        }
      }

      "return multiple errors" when {
        "request supplied has multiple errors" in {
          validator.validate(
            AmendForeignPropertyPeriodSummaryRawData(
              nino = "A12344A",
              businessId = "20178",
              taxYear = taxYear,
              submissionId = validSubmissionId,
              body = requestBodyJson)) shouldBe
            List(NinoFormatError, BusinessIdFormatError)
        }
      }
    }
  }

}
