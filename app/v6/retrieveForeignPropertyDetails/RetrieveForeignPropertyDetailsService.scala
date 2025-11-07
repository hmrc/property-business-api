/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyDetails

import cats.implicits.*
import common.models.errors.PropertyIdFormatError
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.model.response.RetrieveForeignPropertyDetailsResponse
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveForeignPropertyDetailsService @Inject() (connector: RetrieveForeignPropertyDetailsConnector) extends BaseService {

  def retrieveForeignPropertyDetails(request: RetrieveForeignPropertyDetailsRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveForeignPropertyDetailsResponse]] =
    connector.retrieveForeignPropertyDetails(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "1215" -> NinoFormatError,
      "1007" -> BusinessIdFormatError,
      "1117" -> TaxYearFormatError,
      "1244" -> PropertyIdFormatError,
      "1216" -> InternalError,
      "5010" -> NotFoundError,
      "5000" -> RuleTaxYearNotSupportedError
    )
}
