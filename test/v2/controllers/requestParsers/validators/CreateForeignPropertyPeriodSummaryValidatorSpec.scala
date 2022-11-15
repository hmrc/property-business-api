/*
 * Copyright 2022 HM Revenue & Customs
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

import mocks.MockAppConfig
import play.api.libs.json.{ JsArray, JsValue, Json }
import support.UnitSpec
import v2.models.errors._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRawData

class CreateForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"

  MockAppConfig.minimumTaxV2Foreign returns 2021

  private def entryWith(countryCode: String) =
    Json.parse(
      s"""
         |{
         |         "countryCode": "$countryCode",
         |         "income":{
         |            "rentIncome":{
         |               "rentAmount":4882.23
         |            },
         |            "foreignTaxCreditRelief":true,
         |            "premiumsOfLeaseGrant":884.72,
         |            "otherPropertyIncome":7713.09,
         |            "foreignTaxPaidOrDeducted":884.12,
         |            "specialWithholdingTaxOrUkTaxPaid":847.72
         |         },
         |         "expenses":{
         |            "premisesRunningCosts":129.35,
         |            "repairsAndMaintenance":7490.32,
         |            "financialCosts":5000.99,
         |            "professionalFees":847.90,
         |            "travelCosts":69.20,
         |            "costOfServices":478.23,
         |            "residentialFinancialCost":879.28,
         |            "broughtFwdResidentialFinancialCost":846.13,
         |            "other":138.92
         |         }
         |}
         |""".stripMargin
    )

  private val entry = entryWith(countryCode = "AFG")

  private def bodyWith(nonFhlEntries: JsValue*) =
    Json.parse(
      s"""
         |{
         |   "fromDate":"2020-03-29",
         |   "toDate":"2021-03-29",
         |   "foreignFhlEea":{
         |      "income":{
         |         "rentAmount":381.21
         |      },
         |      "expenses":{
         |         "premisesRunningCosts":993.31,
         |         "repairsAndMaintenance":8842.23,
         |         "financialCosts":994,
         |         "professionalFees":992.12,
         |         "costOfServices":4620.23,
         |         "travelCosts":774,
         |         "other":984.41
         |      }
         |   },
         |   "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
         |}
         |""".stripMargin
    )

  private val requestBodyJson = bodyWith(entry)

  private def entryConsolidated = Json.parse(
    """
      |{
      |     "countryCode":"AFG",
      |     "income":{
      |        "rentIncome":{
      |           "rentAmount":4882.23
      |        },
      |        "foreignTaxCreditRelief":true,
      |        "premiumsOfLeaseGrant":884.72,
      |        "otherPropertyIncome":7713.09,
      |        "foreignTaxPaidOrDeducted":884.12,
      |        "specialWithholdingTaxOrUkTaxPaid":847.72
      |     },
      |     "expenses":{
      |        "consolidatedExpenses":129.35
      |     }
      |}
      |""".stripMargin
  )

  private def consolidatedBodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""
      |{
      |   "fromDate": "2020-01-01",
      |   "toDate": "2020-01-31",
      |   "foreignFhlEea":{
      |      "income":{
      |         "rentAmount":381.21
      |      },
      |      "expenses":{
      |         "consolidatedExpenses":993.31
      |      }
      |   },
      |   "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
      |}
      |""".stripMargin
  )

  private val requestBodyConsolidationExpenseJson = consolidatedBodyWith(entryConsolidated)

  val validator = new CreateForeignPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a valid consolidatedExpenses request is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, requestBodyConsolidationExpenseJson)) shouldBe Nil
      }
      "a minimal foreignFhlEea request is supplied" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |    "rentAmount": 567.83
            |    }
            |  }
            |}
            |""".stripMargin)
          )) shouldBe Nil
      }
      "a minimal foreignNonFhlProperty request is supplied" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true
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
        validator.validate(CreateForeignPropertyPeriodSummaryRawData("A12344A", validBusinessId, validTaxYear, requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, "20178", validTaxYear, requestBodyJson)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid taxYear is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "20231", requestBodyJson)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "a taxYear with a range higher than 1 is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2021-23", requestBodyJson)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a taxYear that's before 2021 is supplied" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2020-21", requestBodyJson)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, Json.parse("""{}"""))) shouldBe List(
          RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignFhlEea is submitted" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {}
            |}
            |""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea"))))
      }
      "foreignFhlEea.expenses is empty" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "expenses": {}
            |  }
            |}
            |""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea/expenses"))))
      }
      "foreignFhlEea.income is empty" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {}
            |  }
            |}
            |""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea/income"))))
      }
      "an empty foreignNonFhlProperty is submitted" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": []
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty"))))
      }
      "a foreignNonFhlProperty array is submitted with an empty body in it" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {}
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }
      "foreignNonFhlProperty[].expenses is empty" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode":"AFG",
            |      "expenses": {}
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses"))))
      }
      "foreignNonFhlProperty[].income is empty" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode":"AFG",
            |      "income": {}
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/foreignTaxCreditRelief"))))
      }
      "foreignNonFhlProperty[].income and foreignNonFhlProperty[].expenses are missing but countryCode exists" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode":"AFG"
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0"))))
      }
      "foreignNonFhlProperty[].income.rentIncome is empty" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode":"AFG",
            |      "income": {
            |         "rentIncome": {},
            |         "foreignTaxCreditRelief" : true
            |      }
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/rentIncome"))))
      }
    }

    "return Date Errors" when {
      "the fromDate format is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |   "fromDate":"01-01-2023",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(FromDateFormatError)
      }
      "the toDate format is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |   "fromDate":"2020-01-01",
            |   "toDate":"2020.10.01",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(ToDateFormatError)

      }
      "toDate is before fromDate" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |   "fromDate":"2020-01-31",
            |   "toDate":"2020-01-01",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(RuleToDateBeforeFromDateError)
      }
    }

    "return RuleDuplicateCountryCodeError" when {
      "a country code is duplicated" in {
        val code = "ZWE"
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = validTaxYear,
            body = bodyWith(entryWith(code), entryWith(code))
          )) shouldBe List(
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
      }

      "multiple country codes are duplicated" in {
        val code1 = "AFG"
        val code2 = "ZWE"
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = validTaxYear,
            body = bodyWith(entryWith(code1), entryWith(code2), entryWith(code1), entryWith(code2))
          )) should contain theSameElementsAs List(
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code1, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code2, paths = Seq("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode")),
        )
      }
    }

    "return ValueFormatError" when {
      "foreignFhlEea/income/rentAmount is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.211
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
        )
      }
      "foreignFhlEea/expenses/premisesRunningCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.311,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/premisesRunningCosts")))
        )
      }
      "foreignFhlEea/expenses/repairsAndMaintenance is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.231,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/repairsAndMaintenance")))
        )
      }
      "foreignFhlEea/expenses/financialCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994.231,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/financialCosts")))
        )
      }
      "foreignFhlEea/expenses/professionalFees is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.112,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/professionalFees")))
        )
      }
      "foreignFhlEea/expenses/costOfServices is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.231,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/costOfServices")))
        )
      }
      "foreignFhlEea/expenses/travelCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774.321,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/travelCosts")))
        )
      }
      "foreignFhlEea/expenses/other is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.411
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/other")))
        )
      }
      "foreignFhlEea/expenses/consolidatedExpenses is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "consolidatedExpenses": 567.673
            |    }
            |
            |  },
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/consolidatedExpenses")))
        )
      }
      "foreignNonFhlProperty/0/income/rentIncome/rentAmount is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.231
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/rentIncome/rentAmount")))
        )
      }
      "foreignNonFhlProperty/0/income/premiumsOfLeaseGrant is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.721,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant")))
        )
      }
      "foreignNonFhlProperty/0/income/otherPropertyIncome is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.091,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/otherPropertyIncome")))
        )
      }
      "foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.121,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted")))
        )
      }
      "foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.721
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid")))
        )
      }
      "foreignNonFhlProperty/0/expenses/premisesRunningCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.351,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/premisesRunningCosts")))
        )
      }
      "foreignNonFhlProperty/0/expenses/repairsAndMaintenance is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.321,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/repairsAndMaintenance")))
        )
      }
      "foreignNonFhlProperty/0/expenses/financialCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.991,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/financialCosts")))
        )
      }
      "foreignNonFhlProperty/0/expenses/professionalFees is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.901,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/professionalFees")))
        )
      }
      "foreignNonFhlProperty/0/expenses/costOfServices is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.231,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/costOfServices")))
        )
      }
      "foreignNonFhlProperty/0/expenses/travelCosts is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.201,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.92
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/travelCosts")))
        )
      }
      "foreignNonFhlProperty/0/expenses/other is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |   "fromDate":"2020-03-29",
            |   "toDate":"2021-03-29",
            |   "foreignFhlEea":{
            |      "income":{
            |         "rentAmount":381.21
            |      },
            |      "expenses":{
            |         "premisesRunningCosts":993.31,
            |         "repairsAndMaintenance":8842.23,
            |         "financialCosts":994,
            |         "professionalFees":992.12,
            |         "costOfServices":4620.23,
            |         "travelCosts":774,
            |         "other":984.41
            |      }
            |   },
            |   "foreignNonFhlProperty":[
            |      {
            |         "countryCode":"AFG",
            |         "income":{
            |            "rentIncome":{
            |               "rentAmount":4882.23
            |            },
            |            "foreignTaxCreditRelief":true,
            |            "premiumsOfLeaseGrant":884.72,
            |            "otherPropertyIncome":7713.09,
            |            "foreignTaxPaidOrDeducted":884.12,
            |            "specialWithholdingTaxOrUkTaxPaid":847.72
            |         },
            |         "expenses":{
            |            "premisesRunningCosts":129.35,
            |            "repairsAndMaintenance":7490.32,
            |            "financialCosts":5000.99,
            |            "professionalFees":847.90,
            |            "travelCosts":69.20,
            |            "costOfServices":478.23,
            |            "residentialFinancialCost":879.28,
            |            "broughtFwdResidentialFinancialCost":846.13,
            |            "other":138.921
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/other")))
        )
      }
      "foreignNonFhlProperty/0/expenses/residentialFinancialCost is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.223,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/residentialFinancialCost")))
        )
      }
      "foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.003,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")))
        )
      }
      "foreignNonFhlProperty/0/expenses/consolidatedExpenses is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.663
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/consolidatedExpenses")))
        )
      }
      "multiple fields are invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.776,
            |      "other": 567.67
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode": "FRA",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.463,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    },
            |    {
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.3320
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.212,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/foreignFhlEea/expenses/travelCosts",
            "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant",
            "/foreignNonFhlProperty/1/income/rentIncome/rentAmount",
            "/foreignNonFhlProperty/1/expenses/financialCosts"
          )))
        )
      }
    }

    "return RuleCountryCodeError" when {
      "an invalid country code is provided" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "ABC",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }
      "multiple invalid country codes are provided" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode": "ABC",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    },
            |    {
            |      "countryCode": "DEF",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(
          RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }
    }

    "return CountryCodeFormatError" when {
      "an invalid country code is provided" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "12345678",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }
      "multiple invalid country codes are provided" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode": "12345678",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    },
            |    {
            |      "countryCode": "34567890",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(
          CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }
    }

    "return RuleBothExpensesSuppliedError" when {
      "foreignFhlEea/expenses is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "other": 2425.11,
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/expenses"))))
      }
      "foreignNonFhlProperty/0/expenses is invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "other": 2425.11,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses"))))
      }
      "multiple expenses objects are invalid" in {
        validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
            |{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenses": {
            |      "other": 2425.11,
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignNonFhlProperty": [
            |    {
            |      "countryCode": "FRA",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "other": 2425.11,
            |        "consolidatedExpenses": 352.66
            |      }
            |    },
            |    {
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumsOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenses": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "other": 2425.11,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(
          RuleBothExpensesSuppliedError.copy(
            paths = Some(
              Seq(
                "/foreignFhlEea/expenses",
                "/foreignNonFhlProperty/0/expenses",
                "/foreignNonFhlProperty/1/expenses"
              ))))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(CreateForeignPropertyPeriodSummaryRawData("A12344A", "20178", "232301", requestBodyJson)) shouldBe
          List(NinoFormatError, TaxYearFormatError, BusinessIdFormatError)
      }
    }
  }
}
