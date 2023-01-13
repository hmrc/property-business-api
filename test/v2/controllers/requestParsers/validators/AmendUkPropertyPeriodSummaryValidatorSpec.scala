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

import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v2.models.errors._
import v2.models.request.amendUkPropertyPeriodSummary._
import v2.models.utils.JsonErrorValidators

class AmendUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val taxYear           = "2022-23"
  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  MockAppConfig.minimumTaxV2Uk returns 2021

  private val requestBodyJson = Json.parse(
    """{
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "travelCosts": 974.47,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    }
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    }
      |}
      |""".stripMargin
  )

  val validator = new AmendUkPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, taxYear, validBusinessId, validSubmissionId, requestBodyJson)) shouldBe Nil
      }

      "a valid consolidated expenses request is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino,
                                                               taxYear,
                                                               validBusinessId,
                                                               validSubmissionId,
                                                               requestBodyJsonConsolidatedExpenses)) shouldBe Nil
      }

      "a minimal fhl request is supplied" in {
        validator.validate(
          AmendUkPropertyPeriodSummaryRawData(
            validNino,
            taxYear,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
                |{
                |  "ukFhlProperty": {
                |    "income": {
                |       "periodAmount": 567.83
                |    }
                |  }
                |}
                |""".stripMargin)
          )) shouldBe Nil
      }

      "a minimal non-fhl request is supplied" in {
        validator.validate(
          AmendUkPropertyPeriodSummaryRawData(
            validNino,
            taxYear,
            validBusinessId,
            validSubmissionId,
            Json.parse("""
                |{
                |  "ukNonFhlProperty": {
                |    "income": {
                |      "periodAmount": 567.83
                |    }
                |  }
                |}
                |""".stripMargin)
          )) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData("A12344A", taxYear, validBusinessId, validSubmissionId, requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, "2020", validBusinessId, validSubmissionId, requestBodyJson)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a tax year that is too early is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, "2019-20", validBusinessId, validSubmissionId, requestBodyJson)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "a tax year range is more than 1 year" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, "2019-21", validBusinessId, validSubmissionId, requestBodyJson)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, taxYear, "20178", validSubmissionId, requestBodyJson)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return SubmissionIdFormatError error" when {
      "an invalid submissionId is supplied" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, taxYear, validBusinessId, "12345", requestBodyJson)) shouldBe
          List(SubmissionIdFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(AmendUkPropertyPeriodSummaryRawData(validNino, taxYear, validBusinessId, validSubmissionId, Json.parse("""{}"""))) shouldBe List(
          RuleIncorrectOrEmptyBodyError)
      }

      "an empty object is submitted" when {

        Seq(
          "/ukFhlProperty",
          "/ukFhlProperty/income",
          "/ukFhlProperty/income/rentARoom",
          "/ukFhlProperty/expenses",
          "/ukFhlProperty/expenses/rentARoom",
          "/ukNonFhlProperty",
          "/ukNonFhlProperty/income",
          "/ukNonFhlProperty/income/rentARoom",
          "/ukNonFhlProperty/expenses",
          "/ukNonFhlProperty/expenses/rentARoom",
        ).foreach(p => testEmpty(p))

        def testEmpty(path: String): Unit =
          s"for $path" in {
            validator.validate(
              AmendUkPropertyPeriodSummaryRawData(
                validNino,
                taxYear,
                validBusinessId,
                validSubmissionId,
                requestBodyJson.removeProperty(path).update(path, JsObject.empty)
              )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(path))))
          }
      }

      "an object is empty except for a additional (non-schema) property" in {
        val json = Json.parse("""{
                                |    "ukFhlProperty":{
                                |       "unknownField": 999.99
                                |    }
                                |}""".stripMargin)

        validator.validate(
          AmendUkPropertyPeriodSummaryRawData(
            validNino,
            taxYear,
            validBusinessId,
            validSubmissionId,
            json
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/ukFhlProperty"))))
      }

      "return ValueFormatError" when {
        "income or (non-consolidated) expenses is invalid" when {
          Seq(
            "/ukFhlProperty/income/periodAmount",
            "/ukFhlProperty/income/taxDeducted",
            "/ukFhlProperty/income/rentARoom/rentsReceived",
            "/ukFhlProperty/expenses/premisesRunningCosts",
            "/ukFhlProperty/expenses/repairsAndMaintenance",
            "/ukFhlProperty/expenses/financialCosts",
            "/ukFhlProperty/expenses/professionalFees",
            "/ukFhlProperty/expenses/costOfServices",
            "/ukFhlProperty/expenses/other",
            "/ukFhlProperty/expenses/travelCosts",
            "/ukFhlProperty/expenses/rentARoom/amountClaimed",
            "/ukNonFhlProperty/income/premiumsOfLeaseGrant",
            "/ukNonFhlProperty/income/reversePremiums",
            "/ukNonFhlProperty/income/periodAmount",
            "/ukNonFhlProperty/income/taxDeducted",
            "/ukNonFhlProperty/income/otherIncome",
            "/ukNonFhlProperty/income/rentARoom/rentsReceived",
            "/ukNonFhlProperty/expenses/premisesRunningCosts",
            "/ukNonFhlProperty/expenses/repairsAndMaintenance",
            "/ukNonFhlProperty/expenses/financialCosts",
            "/ukNonFhlProperty/expenses/professionalFees",
            "/ukNonFhlProperty/expenses/costOfServices",
            "/ukNonFhlProperty/expenses/other",
            "/ukNonFhlProperty/expenses/residentialFinancialCost",
            "/ukNonFhlProperty/expenses/travelCosts",
            "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward",
            "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"
          ).foreach(testValueFormatError)

          def testValueFormatError(path: String): Unit = s"for $path" in {
            validator.validate(
              AmendUkPropertyPeriodSummaryRawData(
                validNino,
                taxYear,
                validBusinessId,
                validSubmissionId,
                requestBodyJson.update(path, JsNumber(123.456))
              )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
          }
        }

        "consolidated expenses is invalid" when {
          Seq(
            "/ukFhlProperty/expenses/consolidatedExpenses",
            "/ukNonFhlProperty/expenses/consolidatedExpenses",
          ).foreach(testValueFormatError)

          def testValueFormatError(path: String): Unit = s"for $path" in {
            validator.validate(
              AmendUkPropertyPeriodSummaryRawData(
                validNino,
                taxYear,
                validBusinessId,
                validSubmissionId,
                requestBodyJsonConsolidatedExpenses.update(path, JsNumber(123.456))
              )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
          }
        }

        "multiple fields are invalid" in {
          val path0 = "/ukFhlProperty/expenses/travelCosts"
          val path1 = "/ukNonFhlProperty/expenses/travelCosts"
          val path2 = "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"

          val json = requestBodyJson
            .update(path0, JsNumber(123.456))
            .update(path1, JsNumber(123.456))
            .update(path2, JsNumber(123.456))

          validator.validate(
            AmendUkPropertyPeriodSummaryRawData(
              validNino,
              taxYear,
              validBusinessId,
              validSubmissionId,
              json
            )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path0, path1, path2))))
        }
      }

      "return RuleBothExpensesSuppliedError" when {
        "consolidated and separate expenses provided for fhl" in {
          validator.validate(
            AmendUkPropertyPeriodSummaryRawData(validNino,
                                                taxYear,
                                                validBusinessId,
                                                validSubmissionId,
                                                requestBodyJson.update("ukFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45)))) shouldBe
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/ukFhlProperty/expenses"))))
        }

        "consolidated and separate expenses provided for non-fhl" in {
          validator.validate(
            AmendUkPropertyPeriodSummaryRawData(validNino,
                                                taxYear,
                                                validBusinessId,
                                                validSubmissionId,
                                                requestBodyJson.update("ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45)))) shouldBe
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("/ukNonFhlProperty/expenses"))))
        }
      }

      "return multiple errors" when {
        "request supplied has multiple errors" in {
          validator.validate(AmendUkPropertyPeriodSummaryRawData("A12344A", taxYear, "20178", validSubmissionId, requestBodyJson)) shouldBe
            List(NinoFormatError, BusinessIdFormatError)
        }
      }
    }
  }
}
