/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import config.AppConfig
import play.api.libs.json.{ Json, OWrites, Reads, Writes, __ }
import v2.hateoas.{ HateoasLinks, HateoasListLinksFactory }
import v2.models.domain.HistoricPropertyType
import v2.models.hateoas.{ HateoasData, Link }

case class ListHistoricUkPropertyPeriodSummariesResponse[I](submissions: Seq[I])

object ListHistoricUkPropertyPeriodSummariesResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListHistoricUkPropertyPeriodSummariesResponse[I]] =
    (__ \ "annualAdjustments").read[List[I]].map(ListHistoricUkPropertyPeriodSummariesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListHistoricUkPropertyPeriodSummariesResponse[I]] = Json.writes

  implicit object LinksFactory
      extends HateoasListLinksFactory[ListHistoricUkPropertyPeriodSummariesResponse,
                                      SubmissionPeriod,
                                      ListHistoricUkPropertyPeriodSummariesHateoasData] {
    override def itemLinks(appConfig: AppConfig, data: ListHistoricUkPropertyPeriodSummariesHateoasData, item: SubmissionPeriod): Seq[Link] = {
      import data._

      data.propertyType match {
        case HistoricPropertyType.Fhl =>
          Seq(
            amendHistoricFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value),
            retrieveHistoricFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value)
          )
        case HistoricPropertyType.NonFhl =>
          Seq(
            amendHistoricNonFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value),
            retrieveHistoricNonFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value)
          )
      }
    }

    override def links(appConfig: AppConfig, data: ListHistoricUkPropertyPeriodSummariesHateoasData): Seq[Link] = {
      import data._

      data.propertyType match {
        case HistoricPropertyType.Fhl =>
          Seq(
            listHistoricFhlUkPiePeriodSummaries(appConfig, nino, self = true),
            createHistoricFhlUkPiePeriodSummary(appConfig, nino)
          )
        case HistoricPropertyType.NonFhl =>
          Seq(
            listHistoricNonFhlUkPiePeriodSummaries(appConfig, nino, self = true),
            createHistoricNonFhlUkPiePeriodSummary(appConfig, nino)
          )
      }
    }
  }
}

case class ListHistoricUkPropertyPeriodSummariesHateoasData(nino: String, propertyType: HistoricPropertyType) extends HateoasData
