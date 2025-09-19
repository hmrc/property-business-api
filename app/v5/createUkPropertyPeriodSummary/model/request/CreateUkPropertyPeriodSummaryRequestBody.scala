/*
 * Copyright 2024 HM Revenue & Customs
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

package v5.createUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.utils.EmptinessChecker
import shared.utils.EmptinessChecker.field
import v5.createUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.Def1_Create_UkFhlProperty
import v5.createUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty.Def1_Create_UkNonFhlProperty
import v5.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty.Def2_Create_UkFhlProperty
import v5.createUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.{
  Def2_Create_UkNonFhlProperty,
  Def2_Create_UkNonFhlPropertySubmission
}

sealed trait CreateUkPropertyPeriodSummaryRequestBody

case class Def1_CreateUkPropertyPeriodSummaryRequestBody(fromDate: String,
                                                         toDate: String,
                                                         ukFhlProperty: Option[Def1_Create_UkFhlProperty],
                                                         ukNonFhlProperty: Option[Def1_Create_UkNonFhlProperty])
    extends CreateUkPropertyPeriodSummaryRequestBody

object Def1_CreateUkPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateUkPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    List(
      field("ukFhlProperty", body.ukFhlProperty),
      field("ukNonFhlProperty", body.ukNonFhlProperty)
    )
  }

  implicit val reads: Reads[Def1_CreateUkPropertyPeriodSummaryRequestBody] = Json.reads[Def1_CreateUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "ukFhlProperty").writeNullable[Def1_Create_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def1_Create_UkNonFhlProperty]
  )(o => Tuple.fromProductTyped(o))

}

case class Def2_CreateUkPropertyPeriodSummaryRequestBody(fromDate: String,
                                                         toDate: String,
                                                         ukFhlProperty: Option[Def2_Create_UkFhlProperty],
                                                         ukNonFhlProperty: Option[Def2_Create_UkNonFhlProperty])
    extends CreateUkPropertyPeriodSummaryRequestBody

object Def2_CreateUkPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def2_CreateUkPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    List(
      field("ukFhlProperty", body.ukFhlProperty),
      field("ukNonFhlProperty", body.ukNonFhlProperty)
    )
  }

  implicit val reads: Reads[Def2_CreateUkPropertyPeriodSummaryRequestBody] = Json.reads[Def2_CreateUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def2_CreateUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "ukFhlProperty").writeNullable[Def2_Create_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def2_Create_UkNonFhlProperty]
  )(o => Tuple.fromProductTyped(o))

}

case class Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody(fromDate: String,
                                                                   toDate: String,
                                                                   ukFhlProperty: Option[Def2_Create_UkFhlProperty],
                                                                   ukNonFhlProperty: Option[Def2_Create_UkNonFhlPropertySubmission])
    extends CreateUkPropertyPeriodSummaryRequestBody

object Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody] = EmptinessChecker.use { body =>
    List(
      field("ukFhlProperty", body.ukFhlProperty),
      field("ukNonFhlProperty", body.ukNonFhlProperty)
    )
  }

  implicit val reads: Reads[Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody] =
    Json.reads[Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody]

  implicit val writes: OWrites[Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "ukFhlProperty").writeNullable[Def2_Create_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def2_Create_UkNonFhlPropertySubmission]
  )(o => Tuple.fromProductTyped(o))

}
