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

package v6.amendUkPropertyPeriodSummary.model.request

import common.models.domain.SubmissionId
import shared.models.domain.{BusinessId, Nino, TaxYear}
import v6.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.Def2_Amend_UkNonFhlPropertySubmission

sealed trait AmendUkPropertyPeriodSummaryRequestData {
  val nino: Nino
  def taxYear: TaxYear
  def businessId: BusinessId
  def submissionId: SubmissionId
  def body: AmendUkPropertyPeriodSummaryRequestBody
}

// DEF_1 Models

case class Def1_AmendUkPropertyPeriodSummaryRequestData(nino: Nino,
                                                        taxYear: TaxYear,
                                                        businessId: BusinessId,
                                                        submissionId: SubmissionId,
                                                        body: Def1_AmendUkPropertyPeriodSummaryRequestBody)
    extends AmendUkPropertyPeriodSummaryRequestData

// DEF_2 Models
case class Def2_AmendUkPropertyPeriodSummaryRequestData(nino: Nino,
                                                        taxYear: TaxYear,
                                                        businessId: BusinessId,
                                                        submissionId: SubmissionId,
                                                        body: Def2_AmendUkPropertyPeriodSummaryRequestBody)
    extends AmendUkPropertyPeriodSummaryRequestData {

  def toSubmission: Def2_AmendUkPropertyPeriodSummarySubmissionRequestData = {
    Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(
      nino = nino,
      taxYear = taxYear,
      businessId = businessId,
      submissionId = submissionId,
      body = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(
        body.ukFhlProperty,
        body.ukNonFhlProperty.map(existing => Def2_Amend_UkNonFhlPropertySubmission(existing.income, existing.expenses.map(_.toSubmissionModel)))
      )
    )
  }

}

case class Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(nino: Nino,
                                                                  taxYear: TaxYear,
                                                                  businessId: BusinessId,
                                                                  submissionId: SubmissionId,
                                                                  body: Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody)
    extends AmendUkPropertyPeriodSummaryRequestData
