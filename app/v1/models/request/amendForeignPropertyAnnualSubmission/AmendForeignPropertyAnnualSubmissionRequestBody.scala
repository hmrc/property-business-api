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

package v1.models.request.amendForeignPropertyAnnualSubmission

import play.api.libs.json._
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEea
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty.ForeignPropertyEntry
case class AmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea: Option[ForeignFhlEea], foreignProperty: Option[Seq[ForeignPropertyEntry]]) {
  def isEmpty: Boolean = (foreignFhlEea.isEmpty && foreignProperty.isEmpty) ||
    foreignFhlEea.exists(_.isEmpty) ||
    foreignProperty.exists(_.isEmpty)
}

object AmendForeignPropertyAnnualSubmissionRequestBody {
  implicit val format: OFormat[AmendForeignPropertyAnnualSubmissionRequestBody] = Json.format[AmendForeignPropertyAnnualSubmissionRequestBody]
}
