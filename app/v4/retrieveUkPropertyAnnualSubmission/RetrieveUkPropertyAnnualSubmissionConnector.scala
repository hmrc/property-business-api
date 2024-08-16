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

package v4.retrieveUkPropertyAnnualSubmission

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.outcomes.ResponseWrapper
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionConnector._
import v4.retrieveUkPropertyAnnualSubmission.model.request._
import v4.retrieveUkPropertyAnnualSubmission.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveUkPropertyAnnualSubmissionConnector {

  sealed trait Result

  case class UkResult(response: Def1_RetrieveUkPropertyAnnualSubmissionResponse) extends Result

  case object NonUkResult extends Result
}

@Singleton
class RetrieveUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkProperty(request: RetrieveUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    request match {
      case def1: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =>
        import def1._

        val (downstreamUri, queryParams) = if (taxYear.isTys) {
          (
            TaxYearSpecificIfsUri[Def1_RetrieveUkPropertyAnnualSubmissionResponse](
              s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"),
            Nil
          )
        } else {
          (
            IfsUri[Def1_RetrieveUkPropertyAnnualSubmissionResponse](s"income-tax/business/property/annual"),
            List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear.asMtd)
          )
        }

        val response = get(downstreamUri, queryParams)

        response.map {
          case Right(ResponseWrapper(corId, resp)) if ukResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
          case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
          case Left(e)                                               => Left(e)
        }
    }
  }

  // The same API#1598 IF endpoint is used for both uk and foreign properties.
  // If a businessId of the right type is specified some of these optional fields will be present...
  private def ukResult(response: Def1_RetrieveUkPropertyAnnualSubmissionResponse): Boolean =
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty

}
