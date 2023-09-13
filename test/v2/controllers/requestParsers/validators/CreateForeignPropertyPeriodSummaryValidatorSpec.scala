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
import play.api.libs.json.{JsArray, JsValue, Json}
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

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode"))))
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

        result shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/foreignTaxCreditRelief"))))
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
      "the fromDate format is invalid" in {
        val validator = setUpValidator()

        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(fromDate = "01-01-2023")
          ))

        result shouldBe List(FromDateFormatError)
      }

      "the fromDate is out of range" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(fromDate = "1782-09-04")
          )
        )
        result shouldBe List(FromDateOutOfRangeError)
      }

      "the toDate format is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(toDate = "2020.10.01")
          ))
        result shouldBe List(ToDateFormatError)

      }

      "the toDate is out of range" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(toDate = "3054-03-29")
          ))
        result shouldBe List(ToDateOutOfRangeError)

      }

      "toDate is before fromDate" in {
        val validator = setUpValidator()

        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(fromDate = "2020-01-31", toDate = "2020-01-01")
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
            .forDuplicatedCodesAndPaths(code = code, paths = List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
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
            .forDuplicatedCodesAndPaths(code = code1, paths = List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
          RuleDuplicateCountryCodeError
            .forDuplicatedCodesAndPaths(code = code2, paths = List("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode"))
        )
      }
    }

    "return ValueFormatError" when {
      "foreignFhlEea/income/rentAmount is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaRentAmount = "4882.233")
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/income/rentAmount")))
        )
      }

      "foreignFhlEea/expenses/premisesRunningCosts is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaPremisesRunningCosts = "211.237")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/premisesRunningCosts")))
        )
      }

      "foreignFhlEea/expenses/repairsAndMaintenance is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaRepairsAndMaintenance = "8842.231")
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/repairsAndMaintenance")))
        )
      }

      "foreignFhlEea/expenses/financialCosts is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaFinancialCosts = "42.768")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/financialCosts")))
        )
      }

      "foreignFhlEea/expenses/professionalFees is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaProfessionalFees = "992.112")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/professionalFees")))
        )
      }

      "foreignFhlEea/expenses/costOfServices is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaCostOfServices = "4620.231")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/costOfServices")))
        )
      }

      "foreignFhlEea/expenses/travelCosts is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlEeaTravelCosts = "482.678")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/travelCosts")))
        )
      }

      "foreignFhlEea/expenses/other is invalid" in {
        val validator: CreateForeignPropertyPeriodSummaryValidator = setUpValidator()
        val result: List[MtdError] = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignFhlOther = "482.231")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/other")))
        )
      }

      "foreignFhlEea/expenses/consolidatedExpenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            foreignFhlInvalidConsolidatedExpenses
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/consolidatedExpenses")))
        )
      }

      "foreignNonFhlProperty/0/income/rentIncome/rentAmount is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlRentAmount = "482.231")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/rentIncome/rentAmount")))
        )
      }

      "foreignNonFhlProperty/0/income/premiumsOfLeaseGrant is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyPremiumsOfLeaseGrant = "482.231")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant")))
        )
      }

      "foreignNonFhlProperty/0/income/otherPropertyIncome is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyOtherPropertyIncome = "7713.091")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/otherPropertyIncome")))
        )
      }

      "foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyForeignTaxPaidOrDeducted = "7713.091")
          ))
        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted")))
        )
      }

      "foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertySpecialWithholdingTaxOrUkTaxPaid = "847.721")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid")))
        )
      }

      "foreignNonFhlProperty/0/expenses/premisesRunningCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyPremisesRunningCosts = "847.721")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/premisesRunningCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/repairsAndMaintenance is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyRepairsAndMaintenance = "847.721")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/repairsAndMaintenance")))
        )
      }

      "foreignNonFhlProperty/0/expenses/financialCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyFinancialCosts = "5000.991")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/financialCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/professionalFees is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyProfessionalFees = "847.901")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/professionalFees")))
        )
      }

      "foreignNonFhlProperty/0/expenses/costOfServices is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlCostOfServices = "478.231")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/costOfServices")))
        )
      }

      "foreignNonFhlProperty/0/expenses/travelCosts is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyTravelCosts = "69.201")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/travelCosts")))
        )
      }

      "foreignNonFhlProperty/0/expenses/other is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlOther = "138.921")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/other")))
        )
      }

      "foreignNonFhlProperty/0/expenses/residentialFinancialCost is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlResidentialFinancialCost = "21235.223")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/residentialFinancialCost")))
        )
      }

      "foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlBroughtFwdResidentialFinancialCost = "12556.003")
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")))
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
          ValueFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses/consolidatedExpenses")))
        )
      }

      "multiple fields are invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(
              foreignNonFhlPropertyPremiumsOfLeaseGrant = "2543.463",
              foreignFhlEeaRentAmount = "98765.673",
              foreignNonFhlPropertyTravelCosts = "4325.3243",
              foreignNonFhlPropertyFinancialCosts = "12.901"
            )
          ))

        result shouldBe List(
          ValueFormatError.copy(paths = Some(List(
            "/foreignFhlEea/income/rentAmount",
            "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant",
            "/foreignNonFhlProperty/0/expenses/financialCosts",
            "/foreignNonFhlProperty/0/expenses/travelCosts"
          ))))
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
            requestBody(foreignNonFhlPropertyCountryCode0 = "JUY")
          ))

        result shouldBe List(RuleCountryCodeError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode"))))
      }

      "multiple invalid country codes are provided" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyCountryCode0 = "ABC", foreignNonFhlPropertyCountryCode1 = "DEF")
          ))

        result shouldBe List(RuleCountryCodeError.withPaths(List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
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
            requestBody(foreignNonFhlPropertyCountryCode0 = "12345678")
          ))

        result shouldBe List(CountryCodeFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode"))))
      }

      "multiple invalid country codes are provided" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            requestBody(foreignNonFhlPropertyCountryCode0 = "12345678", foreignNonFhlPropertyCountryCode1 = "34567890")
          ))

        result shouldBe List(
          CountryCodeFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
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
            invalidForeignFhlEeaExpenses
          ))
        result shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(List("/foreignFhlEea/expenses"))))
      }

      "foreignNonFhlProperty/0/expenses is invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            invalidForeignNonFhlPropertyExpenses0
          ))
        result shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(List("/foreignNonFhlProperty/0/expenses"))))
      }

      "multiple expenses objects are invalid" in {
        val validator = setUpValidator()
        val result = validator.validate(
          CreateForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            multipleInvalidExpenses
          ))
        result shouldBe List(
          RuleBothExpensesSuppliedError.copy(
            paths = Some(
              List(
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

  private def requestBody(
      fromDate: String = "2021-01-01",
      toDate: String = "2021-03-29",
      foreignNonFhlRentAmount: String = "4823.89",
      foreignFhlEeaRentAmount: String = "381.21",
      foreignFhlEeaPremisesRunningCosts: String = "993.31",
      foreignFhlEeaRepairsAndMaintenance: String = "8842.23",
      foreignFhlEeaFinancialCosts: String = "994",
      foreignFhlEeaProfessionalFees: String = "992.12",
      foreignFhlEeaCostOfServices: String = "4620.23",
      foreignFhlEeaTravelCosts: String = "774",
      foreignFhlOther: String = "984.41",
      foreignNonFhlPropertyPremiumsOfLeaseGrant: String = "884.72",
      foreignNonFhlPropertyOtherPropertyIncome: String = "7713.09",
      foreignNonFhlPropertyForeignTaxPaidOrDeducted: String = "7713.09",
      foreignNonFhlPropertySpecialWithholdingTaxOrUkTaxPaid: String = "847.72",
      foreignNonFhlPropertyPremisesRunningCosts: String = "139.25",
      foreignNonFhlPropertyRepairsAndMaintenance: String = "7490.32",
      foreignNonFhlPropertyFinancialCosts: String = "5000.99",
      foreignNonFhlPropertyProfessionalFees: String = "847.90",
      foreignNonFhlPropertyTravelCosts: String = "69.20",
      foreignNonFhlCostOfServices: String = "478.23",
      foreignNonFhlResidentialFinancialCost: String = "879.28",
      foreignNonFhlBroughtFwdResidentialFinancialCost: String = "846.13",
      foreignNonFhlOther: String = "138.92",
      foreignNonFhlPropertyCountryCode0: String = "AFG",
      foreignNonFhlPropertyCountryCode1: String = "GBR"
  ): JsValue =
    Json.parse(s"""
                  |{
                  |   "fromDate":"$fromDate",
                  |   "toDate":"$toDate",
                  |   "foreignFhlEea":{
                  |      "income":{
                  |         "rentAmount": "$foreignFhlEeaRentAmount"
                  |      },
                  |      "expenses":{
                  |         "premisesRunningCosts": "$foreignFhlEeaPremisesRunningCosts",
                  |         "repairsAndMaintenance": "$foreignFhlEeaRepairsAndMaintenance",
                  |         "financialCosts": "$foreignFhlEeaFinancialCosts",
                  |         "professionalFees": "$foreignFhlEeaProfessionalFees",
                  |         "costOfServices": "$foreignFhlEeaCostOfServices",
                  |         "travelCosts": "$foreignFhlEeaTravelCosts",
                  |         "other": "$foreignFhlOther"
                  |      }
                  |   },
                  |   "foreignNonFhlProperty":[
                  |      {
                  |         "countryCode":"$foreignNonFhlPropertyCountryCode0",
                  |         "income":{
                  |            "rentIncome":{
                  |               "rentAmount": "$foreignNonFhlRentAmount"
                  |            },
                  |            "foreignTaxCreditRelief":true,
                  |            "premiumsOfLeaseGrant": "$foreignNonFhlPropertyPremiumsOfLeaseGrant",
                  |            "otherPropertyIncome": "$foreignNonFhlPropertyOtherPropertyIncome",
                  |            "foreignTaxPaidOrDeducted": "$foreignNonFhlPropertyForeignTaxPaidOrDeducted",
                  |            "specialWithholdingTaxOrUkTaxPaid": "$foreignNonFhlPropertySpecialWithholdingTaxOrUkTaxPaid"
                  |         },
                  |         "expenses":{
                  |            "premisesRunningCosts": "$foreignNonFhlPropertyPremisesRunningCosts",
                  |            "repairsAndMaintenance":"$foreignNonFhlPropertyRepairsAndMaintenance",
                  |            "financialCosts": "$foreignNonFhlPropertyFinancialCosts",
                  |            "professionalFees": "$foreignNonFhlPropertyProfessionalFees",
                  |            "travelCosts": "$foreignNonFhlPropertyTravelCosts",
                  |            "costOfServices": "$foreignNonFhlCostOfServices",
                  |            "residentialFinancialCost": "$foreignNonFhlResidentialFinancialCost",
                  |            "broughtFwdResidentialFinancialCost": "$foreignNonFhlBroughtFwdResidentialFinancialCost",
                  |            "other": "$foreignNonFhlOther"
                  |         }
                  |      },
                  |      {
                  |      "countryCode": "$foreignNonFhlPropertyCountryCode1",
                  |       "income": {
                  |         "rentIncome": {
                  |           "rentAmount": 34456.30
                  |         },
                  |         "foreignTaxCreditRelief": true,
                  |         "premiumsOfLeaseGrant": 2543.43,
                  |         "otherPropertyIncome": 54325.30,
                  |         "foreignTaxTakenOff": 6543.01,
                  |         "specialWithholdingTaxOrUKTaxPaid": 643245.00
                  |       },
                  |       "expenses": {
                  |         "premisesRunningCosts": 5635.43,
                  |         "repairsAndMaintenance": 3456.65,
                  |         "financialCosts": 34532.21,
                  |         "professionalFees": 32465.32,
                  |         "costsOfServices": 2567.21,
                  |         "travelCosts": 2345.76,
                  |         "other": 2425.11
                  |       }
                  |     }
                  |   ]
                  |}
                  |""".stripMargin)

  def foreignFhlInvalidConsolidatedExpenses: JsValue = Json.parse("""
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

  def invalidForeignFhlEeaExpenses: JsValue = Json.parse(
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

  def invalidForeignNonFhlPropertyExpenses0: JsValue = Json.parse(
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

  def multipleInvalidExpenses: JsValue = Json.parse(
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

}
