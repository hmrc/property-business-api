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

package v4.amendUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.Def1_Amend_UkFhlProperty
import v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty.Def1_Amend_UkNonFhlProperty
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty.Def2_Amend_UkFhlProperty
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.{Def2_Amend_UkNonFhlProperty, Def2_Amend_UkNonFhlPropertySubmission}

sealed trait AmendUkPropertyPeriodSummaryRequestBody

case class Def1_AmendUkPropertyPeriodSummaryRequestBody(ukFhlProperty: Option[Def1_Amend_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def1_Amend_UkNonFhlProperty])
    extends AmendUkPropertyPeriodSummaryRequestBody

object Def1_AmendUkPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[Def1_AmendUkPropertyPeriodSummaryRequestBody] = Json.reads[Def1_AmendUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_AmendUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[Def1_Amend_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def1_Amend_UkNonFhlProperty]
  )(o => Tuple.fromProductTyped(o))

}

case class Def2_AmendUkPropertyPeriodSummaryRequestBody(ukFhlProperty: Option[Def2_Amend_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def2_Amend_UkNonFhlProperty])
    extends AmendUkPropertyPeriodSummaryRequestBody

object Def2_AmendUkPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[Def2_AmendUkPropertyPeriodSummaryRequestBody] = Json.reads[Def2_AmendUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def2_AmendUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[Def2_Amend_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def2_Amend_UkNonFhlProperty]
  )(o => Tuple.fromProductTyped(o))

}

case class Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(ukFhlProperty: Option[Def2_Amend_UkFhlProperty],
                                                                  ukNonFhlProperty: Option[Def2_Amend_UkNonFhlPropertySubmission])
    extends AmendUkPropertyPeriodSummaryRequestBody

object Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody {

  implicit val reads: Reads[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody] =
    Json.reads[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody]

  implicit val writes: OWrites[Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[Def2_Amend_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def2_Amend_UkNonFhlPropertySubmission]
  )(o => Tuple.fromProductTyped(o))

}
