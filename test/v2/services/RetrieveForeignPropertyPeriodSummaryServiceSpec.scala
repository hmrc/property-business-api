/*
 * Copyright 2021 HM Revenue & Customs
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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package v2.services
//
//import support.UnitSpec
//import uk.gov.hmrc.http.HeaderCarrier
//import v2.controllers.EndpointLogContext
//import v2.mocks.connectors.MockRetrieveForeignPropertyPeriodSummaryConnector
//import v2.models.domain.Nino
//import v2.models.errors._
//import v2.models.outcomes.ResponseWrapper
//import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
//import v2.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse
//import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
//import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//class RetrieveForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {
//
//  val nino: String = "AA123456A"
//  val businessId: String = "XAIS12345678910"
//  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
//  implicit val correlationId: String = "X-123"
//
//  private val response = RetrieveForeignPropertyPeriodSummaryResponse(
//    "2020-01-01",
//    "2020-01-31",
//    Some(ForeignFhlEea(
//      ForeignFhlEeaIncome(5000.99),
//      Some(ForeignFhlEeaExpenses(
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        None
//      ))
//    )),
//    Some(Seq(ForeignProperty("FRA",
//      ForeignNonFhlPropertyIncome(
//        ForeignNonFhlPropertyRentIncome(5000.99),
//        false,
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99)
//      ),
//      Some(ForeignNonFhlPropertyExpenses(
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        Some(5000.99),
//        None
//      ))))
//    ))
//
//  private val requestData = RetrieveForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, submissionId)
//
//  trait Test extends MockRetrieveForeignPropertyPeriodSummaryConnector {
//    implicit val hc: HeaderCarrier = HeaderCarrier()
//    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
//
//    val service = new RetrieveForeignPropertyPeriodSummaryService(
//      connector = mockRetrieveForeignPropertyConnector
//    )
//  }
//
//  "service" should {
//    "service call successful" when {
//      "return mapped result" in new Test {
//        MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
//          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))
//
//        await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
//      }
//    }
//  }
//
//  "unsuccessful" should {
//    "map errors according to spec" when {
//
//      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
//        s"a $ifsErrorCode error is returned from the service" in new Test {
//
//          MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
//            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))
//
//          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
//        }
//
//      val input = Seq(
//        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
//        "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
//        "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
//        "NO_DATA_FOUND" -> NotFoundError,
//        "SERVER_ERROR" -> DownstreamError,
//        "SERVICE_UNAVAILABLE" -> DownstreamError,
//        "INVALID_CORRELATIONID" -> DownstreamError
//      )
//
//      input.foreach(args => (serviceError _).tupled(args))
//    }
//  }
//}