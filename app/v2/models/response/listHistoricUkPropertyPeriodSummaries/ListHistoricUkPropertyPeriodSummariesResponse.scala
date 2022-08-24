/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import play.api.libs.json.{ Json, OWrites, Reads, __ }

case class ListHistoricUkPropertyPeriodSummariesResponse(submissions: Seq[SubmissionPeriod])

object ListHistoricUkPropertyPeriodSummariesResponse {

  implicit val reads: Reads[ListHistoricUkPropertyPeriodSummariesResponse] =
    (__ \ "annualAdjustments").read[List[SubmissionPeriod]].map(ListHistoricUkPropertyPeriodSummariesResponse(_))

  implicit val writes: OWrites[ListHistoricUkPropertyPeriodSummariesResponse] = Json.writes
}
