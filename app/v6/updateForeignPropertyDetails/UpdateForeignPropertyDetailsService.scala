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

package v6.updateForeignPropertyDetails

import cats.implicits.*
import common.models.errors.*
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateForeignPropertyDetailsService @Inject() (connector: UpdateForeignPropertyDetailsConnector) extends BaseService {

  def updateForeignPropertyDetails(
      request: UpdateForeignPropertyDetailsRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {
    connector.update(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap = Map(
    "1215" -> NinoFormatError,
    "1244" -> PropertyIdFormatError,
    "1117" -> TaxYearFormatError,
    "1000" -> InternalError,
    "1216" -> InternalError,
    "5010" -> NotFoundError,
    "1245" -> RuleDuplicatePropertyNameError,
    "1246" -> RulePropertyOutsidePeriodError,
    "1247" -> RuleEndDateAfterTaxYearEndError,
    "1248" -> RulePropertyBusinessCeasedError,
    "1249" -> RuleMissingEndDetailsError,
    "4200" -> RuleOutsideAmendmentWindowError,
    "5000" -> RuleTaxYearNotSupportedError
  )

}
