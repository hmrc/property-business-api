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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryRawData

class AmendForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec {

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validSubmissionId = "12345678-1234-4123-9123-123456789012"

  private val requestBodyJson = Json.parse("""{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 567.83
      |      },
      |    "expenditure": {
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
      |      "expenditure": {
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
    """.stripMargin)

  private val requestBodyJsonConsolidated = Json.parse("""{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 567.83
      |      },
      |    "expenditure": {
      |      "consolidatedExpenses": 456.98
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
      |      "expenditure": {
      |        "residentialFinancialCost": 21235.22,
      |        "broughtFwdResidentialFinancialCost": 12556.00,
      |        "consolidatedExpenses": 352.66
      |      }
      |    }
      |  ]
      |}
    """.stripMargin)

  val validator = new AmendForeignPropertyPeriodSummaryValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validSubmissionId, requestBodyJson)) shouldBe Nil
      }
      "a valid consolidatedExpenses request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validSubmissionId, requestBodyJsonConsolidated)) shouldBe Nil
      }
      "a minimal foreignFhlEea request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |    }
            |  }
            |}
            |""".stripMargin)
          )) shouldBe Nil
      }
      "a minimal foreignProperty request is supplied" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignProperty": [
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
        validator.validate(AmendForeignPropertyPeriodSummaryRawData("A12344A", validBusinessId, validSubmissionId, requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        validator.validate(AmendForeignPropertyPeriodSummaryRawData(validNino, "20178", validSubmissionId, requestBodyJson)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return SubmissionIdFormatError error" when {
      "an invalid submissionId is supplied" in {
        validator.validate(AmendForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "12345", requestBodyJson)) shouldBe
          List(SubmissionIdFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validSubmissionId, Json.parse("""{}"""))) shouldBe List(
          RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignFhlEea is submitted" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignFhlEea": {}
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "foreignFhlEea.expenditure is empty" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignFhlEea": {
            |    "expenditure": {}
            |  }
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignProperty is submitted" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignProperty": []
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "a foreignProperty array is submitted with an empty body in it" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignProperty": [
            |    {}
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignPropertyEntry is submitted" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignProperty": [
            |    {
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "foreignProperty[].income is empty" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignProperty": [
            |    {
            |      "income": {}
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "foreignProperty[].expenditure is empty" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""{
            |  "foreignProperty": [
            |    {
            |      "expenditure": {}
            |    }
            |  ]
            |}""".stripMargin)
          )) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return ValueFormatError" when {
      "foreignFhlEea/income/rentAmount is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.833
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30,
            |          "taxDeducted": 6334.34
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
        )
      }
      "foreignFhlEea/expenditure/premisesRunningCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.983,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/premisesRunningCosts")))
        )
      }
      "foreignFhlEea/expenditure/repairsAndMaintenance is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.673,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/repairsAndMaintenance")))
        )
      }
      "foreignFhlEea/expenditure/financialCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.953,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/financialCosts")))
        )
      }
      "foreignFhlEea/expenditure/professionalFees is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.653,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/professionalFees")))
        )
      }
      "foreignFhlEea/expenditure/costsOfServices is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.773,
            |      "travelCosts": 456.77,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/costsOfServices")))
        )
      }
      "foreignFhlEea/expenditure/travelCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.773,
            |      "other": 567.67
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/travelCosts")))
        )
      }
      "foreignFhlEea/expenditure/other is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.673
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/other")))
        )
      }
      "foreignFhlEea/expenditure/consolidatedExpenses is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/expenditure/consolidatedExpenses")))
        )
      }
      "foreignProperty/0/income/rentIncome/rentAmount is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.303
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/income/rentIncome/rentAmount")))
        )
      }
      "foreignProperty/0/income/premiumOfLeaseGrant is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.433,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/income/premiumOfLeaseGrant")))
        )
      }
      "foreignProperty/0/income/otherPropertyIncome is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.303,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/income/otherPropertyIncome")))
        )
      }
      "foreignProperty/0/income/foreignTaxTakenOff is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.013,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/income/foreignTaxTakenOff")))
        )
      }
      "foreignProperty/0/income/specialWithholdingTaxOrUKTaxPaid is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.003
            |      },
            |      "expenditure": {
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/income/specialWithholdingTaxOrUKTaxPaid")))
        )
      }
      "foreignProperty/0/expenditure/premisesRunningCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.433,
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/premisesRunningCosts")))
        )
      }
      "foreignProperty/0/expenditure/repairsAndMaintenance is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.653,
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/repairsAndMaintenance")))
        )
      }
      "foreignProperty/0/expenditure/financialCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.213,
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
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/financialCosts")))
        )
      }
      "foreignProperty/0/expenditure/professionalFees is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.323,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/professionalFees")))
        )
      }
      "foreignProperty/0/expenditure/costsOfServices is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.213,
            |        "travelCosts": 2345.76,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/costsOfServices")))
        )
      }
      "foreignProperty/0/expenditure/travelCosts is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.763,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/travelCosts")))
        )
      }
      "foreignProperty/0/expenditure/other is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "other": 2425.113
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/other")))
        )
      }
      "foreignProperty/0/expenditure/residentialFinancialCost is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "consolidatedExpenses": 456.98
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
            |      "expenditure": {
            |        "residentialFinancialCost": 21235.223,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/residentialFinancialCost")))
        )
      }
      "foreignProperty/0/expenditure/broughtFwdResidentialFinancialCost is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "consolidatedExpenses": 456.98
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
            |      "expenditure": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.003,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/broughtFwdResidentialFinancialCost")))
        )
      }
      "foreignProperty/0/expenditure/consolidatedExpenses is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "consolidatedExpenses": 456.98
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
            |      "expenditure": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.663
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)
          )) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/expenditure/consolidatedExpenses")))
        )
      }
      "multiple fields are invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [
            |    {
            |      "countryCode": "GBR",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.463,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
            "/foreignFhlEea/expenditure/travelCosts",
            "/foreignProperty/0/income/premiumOfLeaseGrant",
            "/foreignProperty/1/income/rentIncome/rentAmount",
            "/foreignProperty/1/expenditure/financialCosts"
          )))
        )
      }
    }

    "return RuleCountryCodeError" when {
      "an invalid country code is provided" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "ABC",
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
            |      "expenditure": {
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
          )) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignProperty/0/countryCode"))))
      }
      "multiple invalid country codes are provided" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [
            |    {
            |      "countryCode": "ABC",
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
            |      "expenditure": {
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
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          )) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))))
      }
    }

    "return CountryCodeFormatError" when {
      "an invalid country code is provided" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [{
            |      "countryCode": "12345678",
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
            |      "expenditure": {
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
          )) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignProperty/0/countryCode"))))
      }
      "multiple invalid country codes are provided" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
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
            |  "foreignProperty": [
            |    {
            |      "countryCode": "12345678",
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
            |      "expenditure": {
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
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
          )) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))))
      }
    }

    "return RuleBothExpensesSuppliedError" when {
      "foreignFhlEea/expenditure is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "other": 2425.11,
            |      "consolidatedExpenses": 456.98
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
            |      "expenditure": {
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin
            )
          )) shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/expenditure"))))
      }
      "foreignProperty/0/expenditure is invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "consolidatedExpenses": 456.98
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
            |      "expenditure": {
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
          )) shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/foreignProperty/0/expenditure"))))
      }
      "multiple expenditure objects are invalid" in {
        validator.validate(
          AmendForeignPropertyPeriodSummaryRawData(
            validNino,
            validBusinessId,
            validSubmissionId,
            Json.parse(
              """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "other": 2425.11,
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignProperty": [
            |    {
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
            |      "expenditure": {
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
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
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
                "/foreignFhlEea/expenditure",
                "/foreignProperty/0/expenditure",
                "/foreignProperty/1/expenditure"
              ))))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(AmendForeignPropertyPeriodSummaryRawData("A12344A", "20178", validSubmissionId, requestBodyJson)) shouldBe
          List(NinoFormatError, BusinessIdFormatError)
      }
    }
  }

}
