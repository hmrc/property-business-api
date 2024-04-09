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

package v4.controllers.retrieveUkPropertyAnnualSubmission.model.request

import api.models.domain.{Nino, BusinessId, TaxYear}

sealed trait RetrieveUkPropertyAnnualSubmissionRequestData {
  val nino: Nino
  val businessId: BusinessId
  val taxYear: TaxYear
}

case class Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino: Nino, businessId: BusinessId, taxYear: TaxYear)
    extends RetrieveUkPropertyAnnualSubmissionRequestData