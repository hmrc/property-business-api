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

package v2.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveUkPropertyAnnualSubmissionConnector {

  sealed trait Result

  case class UkResult(response: RetrieveUkPropertyAnnualSubmissionResponse) extends Result

  case object NonUkResult extends Result
}

import v2.connectors.RetrieveUkPropertyAnnualSubmissionConnector._

@Singleton
class RetrieveUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkProperty(request: RetrieveUkPropertyAnnualSubmissionRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request._

    val response = if (taxYear.useTaxYearSpecificApi) {
      get(
        uri = TaxYearSpecificIfsUri[RetrieveUkPropertyAnnualSubmissionResponse](
          s"income-tax/business/property/annual/${taxYear.asTysDownstream}/${nino.nino}/${businessId}")
      )
    } else {
      // Note that MTD tax year format is used
      get(
        uri = IfsUri[RetrieveUkPropertyAnnualSubmissionResponse](s"income-tax/business/property/annual"),
        queryParams = Seq("taxableEntityId" -> nino.value, "incomeSourceId" -> businessId, "taxYear" -> taxYear.asMtd)
      )
    }

    response.map {
      case Right(ResponseWrapper(corId, resp)) if ukResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
      case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
      case Left(e)                                               => Left(e)
    }
  }

  // The same API#1598 IF endpoint is used for both uk and foreign properties.
  // If a businessId of the right type is specified some of these optional fields will be present...
  private def ukResult(response: RetrieveUkPropertyAnnualSubmissionResponse): Boolean =
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty

}
