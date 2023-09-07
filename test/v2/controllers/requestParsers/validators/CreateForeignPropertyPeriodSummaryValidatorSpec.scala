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
import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import support.UnitSpec
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRawData
import v2.models.utils.JsonErrorValidators

class CreateForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"

  def setUpValidator(): CreateForeignPropertyPeriodSummaryValidator = {
    MockAppConfig.minimumTaxV2Foreign returns 2021
    MockAppConfig.minimumFromDate returns 1900
    MockAppConfig.maximumToDate returns 2100
    new CreateForeignPropertyPeriodSummaryValidator(mockAppConfig)
  }

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

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, requestBodyJson))
        result shouldBe Nil
      }

      "a valid consolidatedExpenses request is supplied" in {
        val validator = setUpValidator()
        val result =
          validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, requestBodyConsolidationExpenseJson))
        result shouldBe Nil
      }

      "a minimal foreignFhlEea request is supplied" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe Nil
      }

      "a minimal foreignNonFhlProperty request is supplied" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData("A12344A", validBusinessId, validTaxYear, requestBodyJson))
        result shouldBe
          List(NinoFormatError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, "20178", validTaxYear, requestBodyJson))
        result shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid taxYear is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "20231", requestBodyJson))
        result shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "a taxYear with a range higher than 1 is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2021-23", requestBodyJson))
        result shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a taxYear that's before 2021 is supplied" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2020-21", requestBodyJson))
        result shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, Json.parse("""{}""")))
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }

      "an empty foreignFhlEea is submitted" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea"))))
      }

      "foreignFhlEea.expenses is empty" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea/expenses"))))
      }

      "foreignFhlEea.income is empty" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea/income"))))
      }

      "an empty foreignNonFhlProperty is submitted" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
            |  "fromDate": "2020-01-01",
            |  "toDate": "2020-01-31",
            |  "foreignNonFhlProperty": []
            |}""".stripMargin)
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty"))))
      }

      "a foreignNonFhlProperty array is submitted with an empty body in it" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }

      "foreignNonFhlProperty[].expenses is empty" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses"))))
      }

      "foreignNonFhlProperty[].income is empty" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/foreignTaxCreditRelief"))))
      }

      "foreignNonFhlProperty[].income and foreignNonFhlProperty[].expenses are missing but countryCode exists" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0"))))
      }

      "foreignNonFhlProperty[].income.rentIncome is empty" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/rentIncome"))))
      }
    }

    "return Date Errors" when {
      "the fromDate format is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("fromDate", JsString("01-01-2023"))
          ))

        result shouldBe List(FromDateFormatError)
      }

      "the fromDate is out of range" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("fromDate", JsString("1782-09-04"))
          )
        )
        result shouldBe List(FromDateOutOfRangeError)
      }

      "the toDate format is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("toDate", JsString("2020.10.01"))
          ))
        result shouldBe List(ToDateFormatError)

      }

      "the toDate is out of range" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("toDate", JsString("3054-03-29"))
          ))
        result shouldBe List(ToDateOutOfRangeError)

      }

      "toDate is before fromDate" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("fromDate", JsString("2020-01-31")).update("toDate", JsString("2020-01-01"))
          ))
        result shouldBe List(RuleToDateBeforeFromDateError)
      }
    }

    "return RuleDuplicateCountryCodeError" when {
      "a country code is duplicated" in {
        val code      = "ZWE"
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = validTaxYear,
            body = bodyWith(entryWith(code), entryWith(code))
          ))
        result shouldBe List(
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
      }

      "multiple country codes are duplicated" in {
        val code1     = "AFG"
        val code2     = "ZWE"
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = validTaxYear,
            body = bodyWith(entryWith(code1), entryWith(code2), entryWith(code1), entryWith(code2))
          ))

        result should contain theSameElementsAs List(
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code1, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code2, paths = Seq("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode"))
        )
      }
    }

    "return ValueFormatError" when {
      "foreignFhlEea/income/rentAmount is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/income/rentAmount", JsString("4882.233"))
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
        )
      }

      "foreignFhlEea/expenses/premisesRunningCosts is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/premisesRunningCosts", JsString("211.237"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/premisesRunningCosts")))
        )
      }

      "foreignFhlEea/expenses/repairsAndMaintenance is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/repairsAndMaintenance", JsString("8842.231"))
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/repairsAndMaintenance")))
        )
      }

      "foreignFhlEea/expenses/financialCosts is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/financialCosts", JsString("42.768"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/financialCosts")))
        )
      }

      "foreignFhlEea/expenses/professionalFees is invalid" in new TestData {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/professionalFees", JsString("992.112"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/professionalFees")))
        )
      }

      "foreignFhlEea/expenses/costOfServices is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/costOfServices", JsString("4620.231"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/costOfServices")))
        )
      }

      "foreignFhlEea/expenses/travelCosts is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/travelCosts", JsString("482.678"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/travelCosts")))
        )
      }

      "foreignFhlEea/expenses/other is invalid" in new TestData {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: Seq[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody.update("foreignFhlEea/expenses/other", JsString("482.231"))
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/other")))
        )
      }

      "foreignFhlEea/expenses/consolidatedExpenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))



        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenses/consolidatedExpenses")))
        )
      }

      "foreignNonFhlProperty/0/income/rentIncome/rentAmount is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/rentIncome/rentAmount")))
        )
      }

      "foreignNonFhlProperty/0/income/premiumsOfLeaseGrant is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant")))
        )
      }

      "foreignNonFhlProperty/0/income/otherPropertyIncome is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/otherPropertyIncome")))
        )
      }

      "foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted")))
        )
      }

      "foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid")))
        )
      }

      "foreignNonFhlProperty/0/expenses/premisesRunningCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/premisesRunningCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/repairsAndMaintenance is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/repairsAndMaintenance")))
        )
      }

      "foreignNonFhlProperty/0/expenses/financialCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/financialCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/professionalFees is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/professionalFees")))
        )
      }

      "foreignNonFhlProperty/0/expenses/costOfServices is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/costOfServices")))
        )
      }

      "foreignNonFhlProperty/0/expenses/travelCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/travelCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/other is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/other")))
        )
      }

      "foreignNonFhlProperty/0/expenses/residentialFinancialCost is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/residentialFinancialCost")))
        )
      }

      "foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")))
        )
      }

      "foreignNonFhlProperty/0/expenses/consolidatedExpenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses/consolidatedExpenses")))
        )
      }

      "multiple fields are invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
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
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }

      "multiple invalid country codes are provided" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }
    }

    "return CountryCodeFormatError" when {
      "an invalid country code is provided" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
      }

      "multiple invalid country codes are provided" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
          CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }
    }

    "return RuleBothExpensesSuppliedError" when {
      "foreignFhlEea/expenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/expenses"))))
      }

      "foreignNonFhlProperty/0/expenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/expenses"))))
      }

      "multiple expenses objects are invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
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
          ))
        result shouldBe List(
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
        val validator = setUpValidator()
        val result    = validator.validate(CreateForeignPropertyPeriodSummaryRawData("A12344A", "20178", "232301", requestBodyJson))
        result shouldBe
          List(NinoFormatError, TaxYearFormatError, BusinessIdFormatError)
      }
    }
  }

  trait TestData {

    val requestBody: JsValue = Json.parse("""
      |{
      |   "fromDate":"2021-01-01",
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
      |""".stripMargin)

  }

}
