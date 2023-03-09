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

package v2.models.audit

import play.api.libs.json.{Json, Writes}
import v2.models.auth.UserDetails

case class DeleteUkPropertyAnnualSubmissionAuditDetail(userType: String,
                                                       agentReferenceNumber: Option[String],
                                                       nino: String, taxYear: String,
                                                       `X-CorrelationId`: String,
                                                       response: AuditResponse)

object DeleteUkPropertyAnnualSubmissionAuditDetail {
  implicit val writes: Writes[DeleteUkPropertyAnnualSubmissionAuditDetail] = Json.writes[DeleteUkPropertyAnnualSubmissionAuditDetail]

  def apply(userDetails: UserDetails, nino: String, taxYear: String, `X-CorrelationId`: String,
            response: AuditResponse): DeleteUkPropertyAnnualSubmissionAuditDetail = {

    DeleteUkPropertyAnnualSubmissionAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = nino,
      taxYear = taxYear,
      `X-CorrelationId` = `X-CorrelationId`,
      response = response
    )
  }

}