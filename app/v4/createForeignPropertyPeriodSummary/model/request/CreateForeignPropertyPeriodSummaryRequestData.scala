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

package v4.createForeignPropertyPeriodSummary.model.request

import api.models.domain.{BusinessId, Nino, TaxYear}
import v4.createForeignPropertyPeriodSummary.def1.model.request.Def1_CreateForeignPropertyPeriodSummaryRequestBody

sealed trait CreateForeignPropertyPeriodSummaryRequestData {
  val nino: Nino
  val businessId: BusinessId
  val taxYear: TaxYear
  def body: CreateForeignPropertyPeriodSummaryRequestBody
}

case class Def1_CreateForeignPropertyPeriodSummaryRequestData(nino: Nino,
                                                              businessId: BusinessId,
                                                              taxYear: TaxYear,
                                                              body: Def1_CreateForeignPropertyPeriodSummaryRequestBody)
    extends CreateForeignPropertyPeriodSummaryRequestData