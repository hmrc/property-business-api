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

package v5.createUkPropertyPeriodSummary.model.request

import shared.models.domain.{BusinessId, Nino, TaxYear}
import v5.createUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.Def2_Create_UkNonFhlPropertySubmission

sealed trait CreateUkPropertyPeriodSummaryRequestData {
  val nino: Nino
  val businessId: BusinessId
  val taxYear: TaxYear

  def body: CreateUkPropertyPeriodSummaryRequestBody
}

case class Def1_CreateUkPropertyPeriodSummaryRequestData(nino: Nino,
                                                         businessId: BusinessId,
                                                         taxYear: TaxYear,
                                                         body: Def1_CreateUkPropertyPeriodSummaryRequestBody)
    extends CreateUkPropertyPeriodSummaryRequestData

case class Def2_CreateUkPropertyPeriodSummaryRequestData(nino: Nino,
                                                         businessId: BusinessId,
                                                         taxYear: TaxYear,
                                                         body: Def2_CreateUkPropertyPeriodSummaryRequestBody)
    extends CreateUkPropertyPeriodSummaryRequestData {

  def toSubmission: Def2_CreateUkPropertyPeriodSummarySubmissionRequestData = {
    Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(
      nino = nino,
      taxYear = taxYear,
      businessId = businessId,
      body = Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody(
        body.fromDate,
        body.toDate,
        body.ukFhlProperty,
        body.ukNonFhlProperty.map(existing => Def2_Create_UkNonFhlPropertySubmission(existing.income, existing.expenses.map(_.toSubmissionModel)))
      )
    )
  }

}

case class Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(nino: Nino,
                                                                   businessId: BusinessId,
                                                                   taxYear: TaxYear,
                                                                   body: Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody)
    extends CreateUkPropertyPeriodSummaryRequestData
