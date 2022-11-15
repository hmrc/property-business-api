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

package v1.controllers.requestParsers

import play.api.libs.json.Json
import support.UnitSpec
import v1.mocks.validators.MockAmendForeignPropertyPeriodSummaryValidator
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.request.common.foreignPropertyEntry._
import v1.models.request.amendForeignPropertyPeriodSummary._
import v1.models.request.common.foreignFhlEea._

class AmendForeignPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val businessId: String             = "XAIS12345678901"
  val submissionId: String           = "12345678-1234-4123-9123-123456789012"
  implicit val correlationId: String = "X-123"

  private val requestBodyJson = Json.parse("""{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 567.83,
      |      "taxDeducted": 4321.92
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
    """.stripMargin)

  val inputData: AmendForeignPropertyPeriodSummaryRawData =
    AmendForeignPropertyPeriodSummaryRawData(nino, businessId, submissionId, requestBodyJson)

  trait Test extends MockAmendForeignPropertyPeriodSummaryValidator {
    lazy val parser = new AmendForeignPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendForeignPropertyValidator.validate(inputData).returns(Nil)

        val foreignFhlEea: ForeignFhlEea = ForeignFhlEea(
          income = Some(ForeignFhlEeaIncome(Some(567.83))),
          expenditure = Some(
            ForeignFhlEeaExpenditure(
              premisesRunningCosts = Some(4567.98),
              repairsAndMaintenance = Some(98765.67),
              financialCosts = Some(4566.95),
              professionalFees = Some(23.65),
              costsOfServices = Some(4567.77),
              travelCosts = Some(456.77),
              other = Some(567.67),
              consolidatedExpenses = None
            ))
        )

        val foreignProperty: ForeignPropertyEntry = ForeignPropertyEntry(
          countryCode = "GBR",
          income = Some(
            ForeignPropertyIncome(
              rentIncome = Some(ForeignPropertyRentIncome(rentAmount = Some(34456.30))),
              foreignTaxCreditRelief = true,
              premiumOfLeaseGrant = Some(2543.43),
              otherPropertyIncome = Some(54325.30),
              foreignTaxTakenOff = Some(6543.01),
              specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
            )),
          expenditure = Some(
            ForeignPropertyExpenditure(
              premisesRunningCosts = Some(5635.43),
              repairsAndMaintenance = Some(3456.65),
              financialCosts = Some(34532.21),
              professionalFees = Some(32465.32),
              costsOfServices = Some(2567.21),
              travelCosts = Some(2345.76),
              residentialFinancialCost = None,
              broughtFwdResidentialFinancialCost = None,
              other = Some(2425.11),
              consolidatedExpenses = None
            ))
        )

        val model: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
          foreignFhlEea = Some(foreignFhlEea),
          foreignProperty = Some(Seq(foreignProperty))
        )

        parser.parseRequest(inputData) shouldBe
          Right(AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, submissionId, model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
