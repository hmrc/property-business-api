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

package v2.models.request.createHistoricFhlUkPiePeriodSummary

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, JsValue, OWrites}
import v2.models.request.RawData
/** Model name abbreviated for convenience.
  *  Create a Historic Furnished Holiday Lettings (Fhl) UK Property Income & Expenses (Pie) Period Summary
  */
case class CreateHistoricFhlUkPiePeriodSummaryRawData(nino: String,body: JsValue) extends RawData


object CreateHistoricFhlUkPiePeriodSummaryRawData{
  implicit val writes: OWrites[CreateHistoricFhlUkPiePeriodSummaryRawData] = (
    (JsPath \ "nino").write[String] and
     (JsPath \ "request").write[JsValue]
    )(unlift(CreateHistoricFhlUkPiePeriodSummaryRawData.unapply))
}
