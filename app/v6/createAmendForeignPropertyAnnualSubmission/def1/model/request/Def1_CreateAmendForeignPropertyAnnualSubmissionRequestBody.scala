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

package v6.createAmendForeignPropertyAnnualSubmission.def1.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignFhlEea.Def1_Create_Amend_ForeignFhlEea
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignProperty.Def1_Create_Amend_ForeignEntry
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestBody

case class Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignFhlEea: Option[Def1_Create_Amend_ForeignFhlEea],
                                                                      foreignProperty: Option[Seq[Def1_Create_Amend_ForeignEntry]])
    extends CreateAmendForeignPropertyAnnualSubmissionRequestBody

object Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody {

  implicit val reads: Reads[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody] =
    Json.reads[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[Def1_Create_Amend_ForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[Def1_Create_Amend_ForeignEntry]]
  )(unlift(Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody.unapply))

}
