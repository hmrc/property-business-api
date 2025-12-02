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

package v6.retrieveForeignPropertyAnnualSubmission.def3.model.response

import play.api.libs.json.{JsPath, Json, OFormat, OWrites, Reads}
import shared.models.domain.Timestamp
import v6.retrieveForeignPropertyAnnualSubmission.model.response.RetrieveForeignPropertyAnnualSubmissionResponse

case class Def3_RetrieveForeignPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    foreignProperty: Seq[RetrieveForeignPropertyEntry]
) extends RetrieveForeignPropertyAnnualSubmissionResponse {

  // Note: foreignProperty is mandatory for Def3 (tax year 26/27 onwards).
  // This response always contains foreign property; UK properties are not applicable.
  // In other words, a successful response will always include foreignProperty entries.
  override def hasForeignData: Boolean = true
}

object Def3_RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val format: OFormat[Def3_RetrieveForeignPropertyAnnualSubmissionResponse] =
    Json.format[Def3_RetrieveForeignPropertyAnnualSubmissionResponse]

}
