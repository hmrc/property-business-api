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

package v2

import api.services.ServiceOutcome
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryResponse
import v2.models.response.createHistoricNonFhlUkPiePeriodSummary.CreateHistoricNonFhlUkPiePeriodSummaryResponse
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryResponse
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryResponse
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

package object services {

  // FHL
  type AmendHistoricFhlUkPiePeriodSummaryServiceOutcome               = ServiceOutcome[Unit]
  type CreateAmendHistoricFhlUkPropertyAnnualSubmissionServiceOutcome = ServiceOutcome[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse]
  type CreateHistoricFhlUkPiePeriodSummaryServiceOutcome              = ServiceOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse]
  type RetrieveHistoricFhlUkPropertyPeriodSummaryServiceOutcome       = ServiceOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse]
  type RetrieveHistoricFhlUkPropertyAnnualSubmissionServiceOutcome    = ServiceOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]

  // Non-FHL
  type AmendHistoricNonFhlUkPiePeriodSummaryServiceOutcome               = ServiceOutcome[Unit]
  type CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionServiceOutcome = ServiceOutcome[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse]
  type CreateHistoricNonFhlUkPropertyPeriodSummaryServiceOutcome         = ServiceOutcome[CreateHistoricNonFhlUkPiePeriodSummaryResponse]
  type RetrieveHistoricNonFhlUkPropertyPeriodSummaryServiceOutcome       = ServiceOutcome[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]
  type RetrieveHistoricNonFhlUkPropertyAnnualSubmissionServiceOutcome    = ServiceOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]

}
