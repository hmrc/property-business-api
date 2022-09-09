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

package v2.hateoas

import config.AppConfig
import v2.models.hateoas.Link
import v2.models.hateoas.Method._

trait HateoasLinks {

  val SELF = "self"

  //Domain URIs

  private def foreignAnnualUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$businessId/annual/$taxYear"

  private def foreignPeriodUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$businessId/period/$taxYear"

  private def foreignPeriodSubmissionUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): String =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$businessId/period/$taxYear/$submissionId"

  private def ukAnnualUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/uk/$nino/$businessId/annual/$taxYear"

  private def ukPeriodUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/uk/$nino/$businessId/period/$taxYear"

  private def ukPeriodSubmissionUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): String =
    s"/${appConfig.apiGatewayContext}/uk/$nino/$businessId/period/$taxYear/$submissionId"

  private def ukHistoricFhlAnnualUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/uk/annual/furnished-holiday-lettings/$nino/$taxYear"

  private def ukHistoricNonFhlAnnualUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/uk/annual/non-furnished-holiday-lettings/$nino/$taxYear"

  private def ukHistoricFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String, maybePeriodId: Option[String]): String = {
    val periodIdPath = maybePeriodId.map(id => s"/$id").getOrElse("")
    s"/${appConfig.apiGatewayContext}/uk/period/furnished-holiday-lettings/$nino$periodIdPath"
  }

  private def ukHistoricNonFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String, maybePeriodId: Option[String]): String = {
    val periodIdPath = maybePeriodId.map(id => s"/$id").getOrElse("")
    s"/${appConfig.apiGatewayContext}/uk/period/non-furnished-holiday-lettings/$nino$periodIdPath"
  }

  // API resource links

  // Foreign
  def createForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignPeriodUri(appConfig, nino, businessId, taxYear), method = POST, rel = "create-foreign-property-period-summary")

  def amendForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): Link =
    Link(href = foreignPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId),
         method = PUT,
         rel = "amend-foreign-property-period-summary")

  def retrieveForeignPropertyPeriodSummary(appConfig: AppConfig,
                                           nino: String,
                                           businessId: String,
                                           taxYear: String,
                                           submissionId: String,
                                           self: Boolean): Link = {
    val rel = if (self) "self" else "retrieve-foreign-property-period-summary"
    Link(href = foreignPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = GET, rel)
  }

  def createAmendForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignAnnualUri(appConfig, nino, businessId, taxYear), method = PUT, rel = "create-and-amend-foreign-property-annual-submission")

  def retrieveForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else "retrieve-foreign-property-annual-submission"
    Link(href = foreignAnnualUri(appConfig, nino, businessId, taxYear), method = GET, rel)
  }

  // UK
  def createUkPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = ukPeriodUri(appConfig, nino, businessId, taxYear), method = POST, rel = "create-uk-property-period-summary")

  def amendUkPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): Link =
    Link(href = ukPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = PUT, rel = "amend-uk-property-period-summary")

  def retrieveUkPropertyPeriodSummary(appConfig: AppConfig,
                                      nino: String,
                                      businessId: String,
                                      taxYear: String,
                                      submissionId: String,
                                      self: Boolean): Link = {
    val rel = if (self) SELF else "retrieve-uk-property-period-summary"
    Link(href = ukPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = GET, rel)
  }

  def createAmendUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = PUT, rel = "create-and-amend-uk-property-annual-submission")

  def retrieveUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else "retrieve-uk-property-annual-submission"
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = GET, rel)
  }

  // Historic UK Annual:

  def retrieveHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else "retrieve-uk-property-historic-fhl-annual-submission"
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = GET, rel)
  }

  def createAmendHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = PUT, rel = "create-and-amend-historic-fhl-uk-property-annual-submission")
  }

  def deleteHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = DELETE, rel = "delete-historic-fhl-uk-property-annual-submission")
  }

  def createAmendHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear),
         method = PUT,
         rel = "create-and-amend-uk-property-historic-non-fhl-annual-submission")
  }

  def retrieveHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else "retrieve-uk-property-historic-non-fhl-annual-submission"
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear), method = GET, rel)
  }

  def listHistoricNonFhlUkPiePeriodSummaries(appConfig: AppConfig, nino: String, self: Boolean): Link = {
    val rel = if (self) SELF else "list-uk-property-historic-non-fhl-period-summaries"
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, None), method = GET, rel)
  }

  def deleteHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear), method = DELETE, rel = "delete-uk-property-historic-non-fhl-annual-submission")
  }

  //Historic UK Periodic
  // FHL:

  def createHistoricFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, None), method = POST, rel = "create-uk-property-historic-fhl-period-summary")

  def amendHistoricFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, Some(periodId)),
         method = PUT,
         rel = "amend-uk-property-historic-fhl-period-summary")

  def retrieveHistoricFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, Some(periodId)), method = GET, rel = SELF)

  def listHistoricFhlUkPiePeriodSummaries(appConfig: AppConfig, nino: String, self: Boolean): Link = {
    val rel = if (self) SELF else "list-uk-property-historic-fhl-period-summaries"
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, None), method = GET, rel)
  }

  // Non-FHL:
  def retrieveHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link = {
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, Some(periodId)), method = GET, rel = SELF)
  }

  def createHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, None), method = POST, rel = "create-uk-property-historic-non-fhl-period-summary")

  def amendHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, Some(periodId)),
         method = PUT,
         rel = "amend-uk-property-historic-non-fhl-period-summary")

  def listUkHistoricNonFHLPiePeriodSummary(appConfig: AppConfig, nino: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, None), method = GET, rel = "list-uk-property-historic-non-fhl-period-summaries")

  // Generic
  def listPropertyPeriodSummaries(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else "list-property-period-summaries"
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$taxYear", method = GET, rel)
  }

  def deletePropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/annual/$taxYear", method = DELETE, rel = "delete-property-annual-submission")
}
