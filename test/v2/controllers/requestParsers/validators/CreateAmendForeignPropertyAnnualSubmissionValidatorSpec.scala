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
import play.api.libs.json._
import support.UnitSpec
import v2.models.errors._
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionRawData
import v2.models.utils.JsonErrorValidators

import javax.inject.Singleton

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val taxYear         = "2021-22"
  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"

  MockAppConfig.minimumTaxV2Foreign returns 2021

  private val structuredBuildingAllowance =
    Json.parse("""{
      |  "amount": 3545.12,
      |  "firstYear": {
      |    "qualifyingDate": "2020-03-29",
      |    "qualifyingAmountExpenditure": 3453.34
      |  },
      |  "building": {
      |    "name": "Building name",
      |    "number": "12",
      |    "postcode": "TF3 4GH"
      |  }
      |}""".stripMargin)

  private def entryWith(countryCode: String, structuredBuildingAllowance: JsValue*) =
    Json.parse(s"""
         |    {
         |      "countryCode": "$countryCode",
         |      "adjustments": {
         |        "privateUseAdjustment": 4553.34,
         |        "balancingCharge": 3453.34
         |      },
         |      "allowances": {
         |        "annualInvestmentAllowance": 38330.95,
         |        "costOfReplacingDomesticItems": 41985.17,
         |        "zeroEmissionsGoodsVehicleAllowance": 9769.19,
         |        "otherCapitalAllowance": 1049.21,
         |        "electricChargePointAllowance": 3565.45,
         |        "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowance)},
         |        "zeroEmissionsCarAllowance": 3456.34
         |      }
         |    }""".stripMargin)

  private def entryWithCountryCode(countryCode: String) = entryWith(countryCode, structuredBuildingAllowance)

  private val entry = entryWithCountryCode(countryCode = "AFG")

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "adjustments": {
       |      "privateUseAdjustment": 34343.45,
       |      "balancingCharge": 53543.23,
       |      "periodOfGraceAdjustment": true
       |    },
       |    "allowances": {
       |      "annualInvestmentAllowance": 3434.23,
       |      "otherCapitalAllowance": 1343.34,
       |      "electricChargePointAllowance": 6565.45,
       |      "zeroEmissionsCarAllowance": 3456.34
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val body = bodyWith(entry)

  private val entryPropertyIncomeAllowance = Json.parse("""
                                               |    {
                                               |      "countryCode": "LBN",
                                               |      "adjustments": {
                                               |        "balancingCharge": 3453.34
                                               |      },
                                               |      "allowances": {
                                               |        "propertyIncomeAllowance": 100.95
                                               |      }
                                               |    }
                                               |""".stripMargin)

  private def propertyIncomeAllowanceBodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "adjustments": {
       |      "periodOfGraceAdjustment": true
       |    },
       |    "allowances": {
       |      "propertyIncomeAllowance": 100.95
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val propertyIncomeAllowanceBody = propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance)

  val validator = new CreateAmendForeignPropertyAnnualSubmissionValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino,
                                                                             businessId = validBusinessId,
                                                                             taxYear = taxYear,
                                                                             body = body)) shouldBe Nil
      }

      "a valid propertyIncomeAllowance request is supplied" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino,
                                                            businessId = validBusinessId,
                                                            taxYear = taxYear,
                                                            body = propertyIncomeAllowanceBody)) shouldBe Nil
      }

      "a minimal fhl request is supplied" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = Json.parse("""
                                |{
                                |  "foreignFhlEea": {
                                |    "adjustments": {
                                |      "periodOfGraceAdjustment": true
                                |    }
                                |  }
                                |}
                                |""".stripMargin)
          )) shouldBe Nil
      }

      "a minimal fhl request with only allowances is supplied" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = Json.parse("""
                                |{
                                |  "foreignFhlEea": {
                                |    "allowances": {
                                |      "annualInvestmentAllowance": 38330.95
                                |    }
                                |  }
                                |}
                                |""".stripMargin)
          )) shouldBe Nil
      }

      "a minimal non-fhl request is supplied" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = Json.parse("""{
                                |  "foreignNonFhlProperty": [
                                |    {
                                |      "countryCode": "LBN",
                                |      "adjustments": {
                                |        "balancingCharge": 12.34
                                |      }
                                |    }
                                |  ]
                                |}
                                |""".stripMargin)
          )) shouldBe Nil
      }

      "a minimal non-fhl request with only allowances is supplied" in {

        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = Json.parse("""{
                                |  "foreignNonFhlProperty": [
                                |    {
                                |      "countryCode": "LBN",
                                |      "allowances": {
                                |        "annualInvestmentAllowance": 38330.95
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
          CreateAmendForeignPropertyAnnualSubmissionRawData(nino = "A12344A", businessId = validBusinessId, taxYear = taxYear, body = body)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino, businessId = validBusinessId, taxYear = "2020", body = body)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a tax year that is too early is supplied" in {
        validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino,
                                                                             businessId = validBusinessId,
                                                                             taxYear = "2020-21",
                                                                             body = body)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "a tax year range is more than 1 year" in {
        validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino,
                                                                             businessId = validBusinessId,
                                                                             taxYear = "2019-21",
                                                                             body = body)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid businessId is supplied" in {
        validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino, businessId = "20178", taxYear = taxYear, body = body)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(nino = validNino,
                                                            businessId = validBusinessId,
                                                            taxYear = taxYear,
                                                            body = Json.parse("""{}"""))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }

      "an object/array is empty or mandatory field is missing" when {
        "fhl is has no allowances or adjustments" in {
          testWith(bodyWith(entry).replaceWithEmptyObject("/foreignFhlEea"), "/foreignFhlEea")
        }

        "fhl has empty allowances" in {
          testWith(bodyWith(entry).replaceWithEmptyObject("/foreignFhlEea/allowances"), "/foreignFhlEea/allowances")
        }

        "fhl has empty adjustments" in {
          testWith(bodyWith(entry).replaceWithEmptyObject("/foreignFhlEea/adjustments"), "/foreignFhlEea/adjustments/periodOfGraceAdjustment")
        }

        "non-fhl has empty entries array" in {
          (bodyWith(Seq.empty: _*), "/foreignNonFhlProperty")
        }

        "non-fhl has entry without country code" in {
          testWith(bodyWith(entry.removeProperty("/countryCode")), "/foreignNonFhlProperty/0/countryCode")
        }

        "non-fhl has entry with no allowances or adjustments" in {
          testWith(bodyWith(entry.removeProperty("/adjustments").removeProperty("/allowances")), "/foreignNonFhlProperty/0")
        }

        "non-fhl has entry with empty adjustments" in {
          testWith(bodyWith(entry.replaceWithEmptyObject("/adjustments")), "/foreignNonFhlProperty/0/adjustments")
        }

        "non-fhl has entry with empty allowances" in {
          testWith(bodyWith(entry.replaceWithEmptyObject("/allowances")), "/foreignNonFhlProperty/0/allowances")
        }

        "non-fhl has empty structuredBuildingAllowance array" in {
          testWith(bodyWith(entryWith("AFG", Seq.empty: _*)), "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance")
        }

        "non-fhl has structuredBuildingAllowance with no amount" in {
          testWith(
            bodyWith(entryWith("AFG", structuredBuildingAllowance.removeProperty("/amount"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/amount"
          )
        }

        "non-fhl has structuredBuildingAllowance with firstYear without qualifyingDate" in {
          testWith(
            bodyWith(entryWith("AFG", structuredBuildingAllowance.removeProperty("/firstYear/qualifyingDate"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
          )
        }

        "non-fhl has structuredBuildingAllowance with firstYear without qualifyingAmountExpenditure" in {
          testWith(
            bodyWith(entryWith("AFG", structuredBuildingAllowance.removeProperty("/firstYear/qualifyingAmountExpenditure"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          )
        }

        "non-fhl has structuredBuildingAllowance without building" in {
          testWith(
            bodyWith(entryWith("AFG", structuredBuildingAllowance.removeProperty("/building/"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building"
          )
        }

        "non-fhl has structuredBuildingAllowance with building without postcode" in {
          testWith(
            bodyWith(entryWith("AFG", structuredBuildingAllowance.removeProperty("/building/postcode"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/postcode"
          )
        }

        def testWith(body: JsValue, expectedPath: String): Unit =
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body
            )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(expectedPath))))
      }

      "an object is empty except for a additional (non-schema) property" in {
        val json = Json.parse("""{
                                |    "foreignFhlEea":{
                                |       "unknownField": 999.99
                                |    }
                                |}""".stripMargin)

        validator.validate(
          CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = json
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignFhlEea"))))
      }

      "return ValueFormatError" when {
        val badValue = JsNumber(123.456)

        "adjustment or allowances (except propertyIncomeAllowance) is invalid" when {
          Seq(
            "/foreignFhlEea/adjustments/privateUseAdjustment",
            "/foreignFhlEea/adjustments/balancingCharge",
            "/foreignFhlEea/allowances/annualInvestmentAllowance",
            "/foreignFhlEea/allowances/otherCapitalAllowance",
            "/foreignFhlEea/allowances/electricChargePointAllowance",
            "/foreignFhlEea/allowances/zeroEmissionsCarAllowance",
          ).foreach(path => testWith(body.update(path, badValue), path))

          Seq(
            (bodyWith(entry.update("/adjustments/privateUseAdjustment", badValue)), "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment"),
            (bodyWith(entry.update("/adjustments/balancingCharge", badValue)), "/foreignNonFhlProperty/0/adjustments/balancingCharge"),
            (bodyWith(entry.update("/allowances/annualInvestmentAllowance", badValue)),
             "/foreignNonFhlProperty/0/allowances/annualInvestmentAllowance"),
            (bodyWith(entry.update("/allowances/costOfReplacingDomesticItems", badValue)),
             "/foreignNonFhlProperty/0/allowances/costOfReplacingDomesticItems"),
            (bodyWith(entry.update("/allowances/zeroEmissionsGoodsVehicleAllowance", badValue)),
             "/foreignNonFhlProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance"),
            (bodyWith(entry.update("/allowances/otherCapitalAllowance", badValue)), "/foreignNonFhlProperty/0/allowances/otherCapitalAllowance"),
            (bodyWith(entry.update("/allowances/electricChargePointAllowance", badValue)),
             "/foreignNonFhlProperty/0/allowances/electricChargePointAllowance"),
            (bodyWith(entry.update("/allowances/zeroEmissionsCarAllowance", badValue)),
             "/foreignNonFhlProperty/0/allowances/zeroEmissionsCarAllowance"),
            (bodyWith(entryWith("AFG", structuredBuildingAllowance.update("/amount", badValue))),
             "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/amount"),
            (bodyWith(entryWith("AFG", structuredBuildingAllowance.update("/firstYear/qualifyingAmountExpenditure", badValue))),
             "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"),
          ).foreach((testWith _).tupled)
        }

        "propertyIncomeAllowance allowances is invalid" when {
          Seq(
            "/foreignFhlEea/allowances/propertyIncomeAllowance",
          ).foreach(path => testForPropertyIncomeAllowance(propertyIncomeAllowanceBody.update(path, badValue), path))

          Seq(
            (propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", badValue)),
             "/foreignNonFhlProperty/0/allowances/propertyIncomeAllowance"),
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }

        "propertyIncomeAllowance allowances is too large" when {
          val bigValue = JsNumber(1000.01)
          Seq(
            "/foreignFhlEea/allowances/propertyIncomeAllowance",
          ).foreach(path => testForPropertyIncomeAllowance(propertyIncomeAllowanceBody.update(path, bigValue), path))

          Seq(
            (propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", bigValue)),
             "/foreignNonFhlProperty/0/allowances/propertyIncomeAllowance"),
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }

        "multiple fields are invalid" in {
          val path0 = "/foreignFhlEea/adjustments/privateUseAdjustment"
          val path1 = "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment"
          val path2 = "/foreignNonFhlProperty/1/allowances/costOfReplacingDomesticItems"

          val json =
            bodyWith(
              entryWith(countryCode = "ZWE", structuredBuildingAllowance).update("/adjustments/privateUseAdjustment", badValue),
              entry.update("/allowances/costOfReplacingDomesticItems", badValue)
            ).update(path0, badValue)

          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = json
            )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path0, path1, path2))))
        }

        def testWith(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = body
            )) shouldBe List(ValueFormatError.copy(paths = Some(Seq(expectedPath))))
        }

        def testForPropertyIncomeAllowance(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = body
            )) shouldBe List(ValueFormatError.forPathAndRange(expectedPath, "0", "1000"))
        }
      }

      "return StringFormatError" when {
        val badStringValue = JsString("x" * 91)

        Seq(
          (bodyWith(entry, entryWith("ZWE", structuredBuildingAllowance.update("/building/postcode", badStringValue))),
           "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/postcode"),
          (bodyWith(entry, entryWith("ZWE", structuredBuildingAllowance.update("/building/number", badStringValue))),
           "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/number"),
          (bodyWith(entry, entryWith("ZWE", structuredBuildingAllowance.update("/building/name", badStringValue))),
           "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/name"),
        ).foreach(p => (testWith _).tupled(p))

        def testWith(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = body
            )) shouldBe List(StringFormatError.copy(paths = Some(Seq(expectedPath))))
        }
      }

      "return DateFormatError" when {
        Seq(
          (bodyWith(entry, entryWith("ZWE", structuredBuildingAllowance.update("/firstYear/qualifyingDate", JsString("9999-99-99")))),
           "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"),
        ).foreach(p => (testWith _).tupled(p))

        def testWith(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = body
            )) shouldBe List(DateFormatError.copy(paths = Some(Seq(expectedPath))))
        }
      }

      "return RuleCountryCodeError" when {
        "an invalid country code is provided" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = bodyWith(entryWithCountryCode("QQQ"))
            )) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
        }

        "multiple invalid country codes are provided" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = bodyWith(entryWithCountryCode("QQQ"), entryWithCountryCode("AAA"))
            )) shouldBe List(
            RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
        }
      }

      "return CountryCodeFormatError" when {
        "an invalid country code is provided" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = bodyWith(entryWithCountryCode("XXXX"))
            )) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode"))))
        }
      }

      "return RuleDuplicateCountryCodeError" when {
        "a country code is duplicated" in {
          val code = "ZWE"
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = bodyWith(entryWithCountryCode(code), entryWithCountryCode(code))
            )) shouldBe List(
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
        }

        "multiple country codes are duplicated" in {
          val code1 = "AFG"
          val code2 = "ZWE"
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entryWithCountryCode(code1), entryWithCountryCode(code2), entryWithCountryCode(code1), entryWithCountryCode(code2))
          )) should contain theSameElementsAs List(
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code1, paths = Seq("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = code2, paths = Seq("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode")),
          )
        }
      }

      "return RuleBothAllowancesSuppliedError" when {
        "propertyIncomeAllowance and separate allowances are provided for fhl" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = body
              .update("/foreignFhlEea/allowances/propertyIncomeAllowance", JsNumber(123.45))
              .removeProperty("/foreignFhlEea/adjustments/privateUseAdjustment")
          )) shouldBe
            List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/allowances"))))
        }

        "propertyIncomeAllowance and separate allowances are provided for non-fhl" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entry
              .update("/allowances/propertyIncomeAllowance", JsNumber(123.45))
              .removeProperty("/adjustments/privateUseAdjustment"))
          )) shouldBe
            List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/allowances"))))
        }
      }

      "return RuleBuildingNameNumberError" when {
        val buildingAllowanceWithoutNameOrNumber =
          structuredBuildingAllowance
            .removeProperty("/building/name")
            .removeProperty("/building/number")

        "only the postcode is supplied in structuredBuildingAllowance" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(
              nino = validNino,
              businessId = validBusinessId,
              taxYear = taxYear,
              body = bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber))
            )) shouldBe List(
            RuleBuildingNameNumberError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building"))))
        }

        "only the postcode is supplied for multiple buildings" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber, buildingAllowanceWithoutNameOrNumber))
          )) shouldBe List(
            RuleBuildingNameNumberError.copy(paths = Some(Seq(
              "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building",
              "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/1/building"
            ))))
        }
      }

      "return no errors" when {
        "a postcode is with a name but not a number" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entryWith("AFG",
                                      structuredBuildingAllowance
                                        .removeProperty("/building/number")))
          )) shouldBe Nil
        }

        "a postcode is with a number but not a name" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entryWith("AFG",
                                      structuredBuildingAllowance
                                        .removeProperty("/building/name")))
          )) shouldBe Nil
        }
      }

      "return RulePropertyIncomeAllowanceError" when {
        "propertyIncomeAllowance is supplied with privateUseAdjustment for fhl" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = propertyIncomeAllowanceBody.update("/foreignFhlEea/adjustments/privateUseAdjustment", JsNumber(123.45))
          )) shouldBe
            List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq("/foreignFhlEea"))))
        }

        "propertyIncomeAllowance is supplied with privateUseAdjustment for non-fhl" in {
          validator.validate(CreateAmendForeignPropertyAnnualSubmissionRawData(
            nino = validNino,
            businessId = validBusinessId,
            taxYear = taxYear,
            body = bodyWith(entryPropertyIncomeAllowance.update("/adjustments/privateUseAdjustment", JsNumber(123.45)))
          )) shouldBe
            List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq("/foreignNonFhlProperty/0"))))
        }
      }

      "return multiple errors" when {
        "request supplied has multiple errors" in {
          validator.validate(
            CreateAmendForeignPropertyAnnualSubmissionRawData(nino = "A12344A", businessId = "20178", taxYear = taxYear, body = body)) shouldBe
            List(NinoFormatError, BusinessIdFormatError)
        }
      }
    }
  }
}
