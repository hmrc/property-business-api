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

package v2.models.request.createAmendForeignPropertyAnnualSubmission

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEea
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl.ForeignNonFhlEntry

case class CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea: Option[ForeignFhlEea],
                                                                 foreignNonFhlProperty: Option[Seq[ForeignNonFhlEntry]])

object CreateAmendForeignPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[CreateAmendForeignPropertyAnnualSubmissionRequestBody] = Json.reads[CreateAmendForeignPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[CreateAmendForeignPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[ForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[ForeignNonFhlEntry]]
  )(unlift(CreateAmendForeignPropertyAnnualSubmissionRequestBody.unapply))
}
