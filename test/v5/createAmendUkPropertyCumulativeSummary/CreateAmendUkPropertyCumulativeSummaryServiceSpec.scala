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

package v5.createAmendUkPropertyCumulativeSummary

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import play.api.libs.json.{JsValue, Json}
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request._
import v5.createAmendUkPropertyCumulativeSummary.model.request.{
  CreateAmendUkPropertyCumulativeSummaryRequestData,
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody,
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestData
}
import v5.createAmendUkPropertyCumulativeSummary.model.response.CreateAmendUkPropertyCumulativeSummaryResponse

import scala.concurrent.Future

class CreateAmendUkPropertyCumulativeSummaryServiceSpec extends ServiceSpec with MockCreateAmendUkPropertyCumulativeSummaryConnector {

  private val nino                                    = "AA123456A"
  private val taxYear                                 = "2020-21"
  private val businessId                              = "XAIS12345678910"
  private val submissionId                            = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit private val correlationId: String          = "X-123"
  implicit private val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

  val requestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
    Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
      fromDate = "2023-04-01",
      toDate = "2024-04-01",
      ukProperty = UkProperty(
        income = Some(
          Income(
            premiumsOfLeaseGrant = Some(42.12),
            reversePremiums = Some(84.31),
            periodAmount = Some(9884.93),
            taxDeducted = Some(842.99),
            otherIncome = Some(31.44),
            rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
          )
        ),
        expenses = Some(
          Expenses(
            premisesRunningCosts = Some(1500.50),
            repairsAndMaintenance = Some(1200.75),
            financialCosts = Some(2000.00),
            professionalFees = Some(500.00),
            costOfServices = Some(300.25),
            other = Some(100.50),
            residentialFinancialCost = Some(9000.10),
            travelCosts = Some(400.00),
            residentialFinancialCostsCarriedForward = Some(300.13),
            rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
            consolidatedExpenses = None
          )
        )
      )
    )

  val responseBodyJson: JsValue = Json.parse(
    s"""
       |{
       |  "submissionId":"4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
       |}
       """.stripMargin
  )

  val responseData: CreateAmendUkPropertyCumulativeSummaryResponse =
    CreateAmendUkPropertyCumulativeSummaryResponse(submissionId = submissionId)

  "CreateAmendUkPropertyCumulativeSummaryService" when {
    "downstream call is successful" when {
      "a submission id is returned from downstream" must {
        "return a successful result" in new Test {
          MockedCreateUkPropertyCumulativeSummaryConnector.createAmendUkPropertyCumulativeSummary(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, responseData)))

          await(service.createAmendUkPropertyCumulativeSummary(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseData))
        }
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in new Test {
            MockedCreateUkPropertyCumulativeSummaryConnector.createAmendUkPropertyCumulativeSummary(requestData) returns
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))

            await(service.createAmendUkPropertyCumulativeSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
          "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
          "INVALID_PAYLOAD"           -> InternalError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError
        )

        errorMap.foreach(args => (serviceError _).tupled(args))
      }
    }

    trait Test extends MockCreateAmendUkPropertyCumulativeSummaryConnector {
      val service = new CreateAmendUkPropertyCumulativeSummaryService(mockCreateUkPropertyCumulativeSummaryConnector)

      protected val requestData: CreateAmendUkPropertyCumulativeSummaryRequestData =
        Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), BusinessId(businessId), requestBody)

    }

  }

}
