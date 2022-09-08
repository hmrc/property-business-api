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

package v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import config.AppConfig
import play.api.libs.json.{ JsPath, Json, OWrites, Reads, __ }
import play.api.libs.functional.syntax._
import v2.hateoas.{ HateoasLinks, HateoasLinksFactory }
import v2.models.hateoas.{ HateoasData, Link }

case class RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(fromDate: String,
                                                            toDate: String,
                                                            income: Option[PeriodIncome],
                                                            expenses: Option[PeriodExpenses])

object RetrieveHistoricNonFhlUkPiePeriodSummaryResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse] = Json.writes[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]

  implicit val reads: Reads[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse] = (
    (JsPath \ "from").read[String] and
      (JsPath \ "to").read[String] and
      (__ \ "financials" \ "incomes").readNullable[PeriodIncome] and
      (JsPath \ "financials" \ "deductions").readNullable[PeriodExpenses]
  )(RetrieveHistoricNonFhlUkPiePeriodSummaryResponse.apply _)

  implicit object RetrieveNonFhlUkPiePeriodSummaryLinksFactory
      extends HateoasLinksFactory[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse, RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData): Seq[Link] = {
      import data._

      Seq(
        amendHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId),
        retrieveHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId),
        listUkHistoricNonFHLPiePeriodSummary(appConfig, nino)
      )
    }
  }
}

case class RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData(nino: String, periodId: String) extends HateoasData
