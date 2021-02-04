/*
 * Copyright 2021 HM Revenue & Customs
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

package v1

import v1.models.errors.ErrorWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.response.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod}
import v1.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse
import v1.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v1.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse


package object services {

  private type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  type CreateForeignPropertyPeriodSummaryServiceOutcome = ServiceOutcome[CreateForeignPropertyPeriodSummaryResponse]

  type AmendForeignPropertyPeriodSummaryServiceOutcome = ServiceOutcome[Unit]

  type RetrieveForeignPropertyPeriodSummaryServiceOutcome = ServiceOutcome[RetrieveForeignPropertyPeriodSummaryResponse]

  type RetrieveForeignPropertyAnnualSubmissionServiceOutcome = ServiceOutcome[RetrieveForeignPropertyAnnualSubmissionResponse]

  type ListForeignPropertiesPeriodSummariesServiceOutcome = ServiceOutcome[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]]

  type AmendForeignPropertyAnnualSubmissionServiceOutcome = ServiceOutcome[Unit]

  type DeleteForeignPropertyAnnualSubmissionServiceOutcome = ServiceOutcome[Unit]

}