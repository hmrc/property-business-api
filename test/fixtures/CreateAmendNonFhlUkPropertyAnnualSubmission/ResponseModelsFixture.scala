/*
 * Copyright 2022 HM Revenue & Customs
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

package fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission

import v2.models.domain.{ Nino, TaxYear }
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest,
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  HistoricNonFhlAnnualAdjustments,
  HistoricNonFhlAnnualAllowances
}

trait ResponseModelsFixture {

  val nino: String              = "AA123456A"
  val taxYear: String           = "2019-20"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  private val annualAdjustments = HistoricNonFhlAnnualAdjustments(
    Some(BigDecimal("105.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("120.11")),
    Some(BigDecimal("120.11")),
    true,
    Some(UkPropertyAdjustmentsRentARoom(true))
  )

  private val annualAllowances = HistoricNonFhlAnnualAllowances(
    Some(BigDecimal("100.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("425.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("425.11")),
    Some(BigDecimal("550.11"))
  )

  val Body: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(
    Some(annualAdjustments),
    Some(annualAllowances)
  )

  val Request = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(
    Nino(nino),
    TaxYear.fromMtd(taxYear),
    Body
  )
}
