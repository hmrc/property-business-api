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

package v2.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import api.models.domain.{Nino, TaxYear}
import v2.controllers.requestParsers.validators.AmendForeignPropertyPeriodSummaryValidator
import v2.models.request.amendForeignPropertyPeriodSummary._

import javax.inject.Inject

class AmendForeignPropertyPeriodSummaryRequestParser @Inject() (val validator: AmendForeignPropertyPeriodSummaryValidator)
    extends RequestParser[AmendForeignPropertyPeriodSummaryRawData, AmendForeignPropertyPeriodSummaryRequest] {

  override protected def requestFor(data: AmendForeignPropertyPeriodSummaryRawData): AmendForeignPropertyPeriodSummaryRequest =
    AmendForeignPropertyPeriodSummaryRequest(
      Nino(data.nino),
      data.businessId,
      TaxYear.fromMtd(data.taxYear),
      data.submissionId,
      data.body.as[AmendForeignPropertyPeriodSummaryRequestBody]
    )

}
