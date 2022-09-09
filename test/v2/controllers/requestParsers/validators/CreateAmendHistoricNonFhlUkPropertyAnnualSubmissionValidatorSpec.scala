/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.controllers.requestParsers.validators

import mocks.MockAppConfig
import play.api.libs.json.{ JsNumber, JsObject, JsValue, Json }
import support.UnitSpec
import v2.models.errors._
import v2.models.utils.JsonErrorValidators

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"
  MockAppConfig.minimumTaxHistoric returns 2017 anyNumberOfTimes ()
  MockAppConfig.maximumTaxHistoric returns 2022 anyNumberOfTimes ()

  val validator = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator(mockAppConfig)

  private def data(nino: String, taxYear: String, body: JsValue) =
    CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, body)

  val body: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 200.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "zeroEmissionGoodsVehicleAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 200.00,
      |      "otherCapitalAllowance": 200.00,
      |      "costOfReplacingDomesticGoods": 200.00,
      |      "propertyIncomeAllowance": 30.02
      |   }
      |}
      |""".stripMargin)

  "The validator" should {
    "return no errors" when {
      "given a complete request" in {
        validator.validate(data(validNino, validTaxYear, body)) shouldBe empty
      }

      "given a request with only annualAdjustments" in {
        validator.validate(data(validNino, validTaxYear, body.removeProperty("/annualAllowances"))) shouldBe empty
      }

      "given a request with only annualAllowances" in {
        validator.validate(data(validNino, validTaxYear, body.removeProperty("/annualAdjustments"))) shouldBe empty
      }
    }

    "return ValueFormatError" when {
      "given numeric amounts" when {
        def testWith(path: String, min: BigDecimal, max: BigDecimal): Unit = s"for $path" when {
          val errors = List(ValueFormatError.forPathAndRange(path, min.toString, max.toString))

          "it is too small" in {
            validator.validate(data(validNino, validTaxYear, body.update(path, JsNumber(min - 0.01)))) shouldBe errors
          }

          "it is too big" in {
            validator.validate(data(validNino, validTaxYear, body.update(path, JsNumber(max + 0.01)))) shouldBe errors
          }

          "it is a bad value" in {
            validator.validate(data(validNino, validTaxYear, body.update(path, JsNumber(123.456)))) shouldBe errors
          }
        }

        val max: BigDecimal  = 99999999999.99
        val zero: BigDecimal = 0

        Seq(
          ("/annualAdjustments/lossBroughtForward", zero, max),
          ("/annualAdjustments/privateUseAdjustment", zero, max),
          ("/annualAdjustments/balancingCharge", zero, max),
          ("/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges", zero, max),
          ("/annualAllowances/annualInvestmentAllowance", zero, max),
          ("/annualAllowances/zeroEmissionGoodsVehicleAllowance", zero, max),
          ("/annualAllowances/businessPremisesRenovationAllowance", zero, max),
          ("/annualAllowances/otherCapitalAllowance", zero, max),
          ("/annualAllowances/costOfReplacingDomesticGoods", zero, max),
          ("/annualAllowances/propertyIncomeAllowance", zero, 1000: BigDecimal)
        ).foreach((testWith _).tupled)
      }

    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty Json object" in {
        validator.validate(data(validNino, validTaxYear, JsObject.empty)) should contain only RuleIncorrectOrEmptyBodyError
      }

      "a mandatory field is missing" when {
        def testWith(path: String): Unit = s"for $path" in {
          validator.validate(data(validNino, validTaxYear, body.removeProperty(path))) should contain only RuleIncorrectOrEmptyBodyError.copy(
            paths = Some(Seq(path)))
        }

        Seq(
          "/annualAdjustments/nonResidentLandlord",
          "/annualAdjustments/rentARoom/jointlyLet"
        ).foreach(testWith)
      }

      "given an empty object" when {
        def testWith(path: String): Unit = s"for $path" in {
          validator.validate(data(validNino, validTaxYear, body.replaceWithEmptyObject(path))) should contain only RuleIncorrectOrEmptyBodyError.copy(
            paths = Some(Seq(path)))
        }

        Seq(
          "/annualAdjustments/nonResidentLandlord",
          "/annualAllowances"
        ).foreach(testWith)

        "for /annualAdjustments" in {
          // Because it has a mandatory property
          validator.validate(data(validNino, validTaxYear, body.replaceWithEmptyObject("/annualAdjustments"))) should contain only RuleIncorrectOrEmptyBodyError
            .copy(paths = Some(Seq("/annualAdjustments/nonResidentLandlord")))
        }
      }
    }

    "return NinoFormatError" when {
      "given an invalid nino" in {
        validator.validate(data("BAD_NINO", validTaxYear, body)) should contain only NinoFormatError
      }
    }

    "return HistoricTaxYearNotSupportedError error" when {
      "given an invalid taxYear" in {
        validator.validate(data(validNino, "BAD_TAX_YEAR", body)) should contain only TaxYearFormatError
      }
    }

    "return HistoricTaxYearNotSupportedError error" when {
      "the tax year is too early" in {
        validator.validate(data(validNino, "2016-17", body)) should contain only RuleHistoricTaxYearNotSupportedError
      }

      "the tax year is too late" in {
        validator.validate(data(validNino, "2023-24", body)) should contain only RuleHistoricTaxYearNotSupportedError
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "given an invalid taxYear range" in {
        validator.validate(data(validNino, "2020-22", body)) should contain only RuleTaxYearRangeInvalidError
      }
    }

    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        validator.validate(data("BAD-NINO", validTaxYear, JsObject.empty)) should contain only NinoFormatError
      }
    }

    "return multiple errors" when {
      "a multiple fields failed validation" in {
        validator.validate(data("BAD_NINO", "BAD_TAX_YEAR", body)) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
