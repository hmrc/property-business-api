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

package v2.models.request.amendForeignPropertyAnnualSubmission

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEea
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignNonFhl.ForeignNonFhlEntry

case class AmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea: Option[ForeignFhlEea],
                                                           foreignNonFhlProperty: Option[Seq[ForeignNonFhlEntry]])

object AmendForeignPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[AmendForeignPropertyAnnualSubmissionRequestBody] = Json.reads[AmendForeignPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[AmendForeignPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[ForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[ForeignNonFhlEntry]]
  )(unlift(AmendForeignPropertyAnnualSubmissionRequestBody.unapply))
}
