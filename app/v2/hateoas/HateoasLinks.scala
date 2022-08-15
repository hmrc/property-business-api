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
import v2.models.hateoas.RelType._

trait HateoasLinks {

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
    s"/${appConfig.apiGatewayContext}/uk/furnished-holiday-lettings/$nino/$taxYear"

  private def ukHistoricNonFhlAnnualUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/uk/non-furnished-holiday-lettings/$nino/$taxYear"

  private def ukHistoricFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String, periodId: String): String =
    s"/${appConfig.apiGatewayContext}/uk/furnished-holiday-lettings/$nino/$periodId"

  private def ukHistoricFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String): String =
    s"/${appConfig.apiGatewayContext}/uk/furnished-holiday-lettings/$nino"

  private def ukHistoricNonFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String, periodId: String): String =
    s"/${appConfig.apiGatewayContext}/uk/non-furnished-holiday-lettings/$nino/$periodId"

  private def ukHistoricNonFhlPiePeriodSummaryUri(appConfig: AppConfig, nino: String): String =
    s"/${appConfig.apiGatewayContext}/uk/non-furnished-holiday-lettings/$nino"
  // API resource links

  // Foreign
  def createForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignPeriodUri(appConfig, nino, businessId, taxYear), method = POST, rel = CREATE_FOREIGN_PROPERTY_PERIOD_SUMMARY)

  def amendForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): Link =
    Link(href = foreignPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId),
         method = PUT,
         rel = AMEND_FOREIGN_PROPERTY_PERIOD_SUMMARY)

  def retrieveForeignPropertyPeriodSummary(appConfig: AppConfig,
                                           nino: String,
                                           businessId: String,
                                           taxYear: String,
                                           submissionId: String,
                                           self: Boolean): Link = {
    val rel = if (self) "self" else RETRIEVE_FOREIGN_PROPERTY_PERIOD_SUMMARY
    Link(href = foreignPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = GET, rel)
  }

  def createAmendForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignAnnualUri(appConfig, nino, businessId, taxYear), method = PUT, rel = CREATE_AND_AMEND_FOREIGN_PROPERTY_ANNUAL_SUBMISSION)

  def retrieveForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else RETRIEVE_FOREIGN_PROPERTY_ANNUAL_SUBMISSION
    Link(href = foreignAnnualUri(appConfig, nino, businessId, taxYear), method = GET, rel)
  }

  // UK
  def createUkPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = ukPeriodUri(appConfig, nino, businessId, taxYear), method = POST, rel = CREATE_UK_PROPERTY_PERIOD_SUMMARY)

  def amendUkPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, submissionId: String): Link =
    Link(href = ukPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = PUT, rel = AMEND_UK_PROPERTY_PERIOD_SUMMARY)

  def retrieveUkPropertyPeriodSummary(appConfig: AppConfig,
                                      nino: String,
                                      businessId: String,
                                      taxYear: String,
                                      submissionId: String,
                                      self: Boolean): Link = {
    val rel = if (self) SELF else RETRIEVE_UK_PROPERTY_PERIOD_SUMMARY
    Link(href = ukPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = GET, rel)
  }

  def createAmendUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = PUT, rel = CREATE_AND_AMEND_UK_PROPERTY_ANNUAL_SUBMISSION)

  def retrieveUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else RETRIEVE_UK_PROPERTY_ANNUAL_SUBMISSION
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = GET, rel)
  }

  // Historic UK Annual:

  def retrieveHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = GET, SELF)
  }

  def createAmendHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AND_AMEND_HISTORIC_FHL_UK_PROPERTY_ANNUAL_SUBMISSION)
  }

  def deleteHistoricFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricFhlAnnualUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_HISTORIC_FHL_UK_PROPERTY_ANNUAL_SUBMISSION)
  }

  def createAmendHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear),
         method = PUT,
         rel = CREATE_AND_AMEND_HISTORIC_NON_FHL_UK_PROPERTY_ANNUAL_SUBMISSION)
  }

  def retrieveHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear), method = GET, SELF)
  }

  def deleteHistoricNonFhlUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, taxYear: String): Link = {
    Link(href = ukHistoricNonFhlAnnualUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_HISTORIC_NON_FHL_UK_PROPERTY_ANNUAL_SUBMISSION)
  }

  //Historic UK Property Income & Expenses Period Summary
  // FHL:

  def amendHistoricFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, periodId), PUT, AMEND_HISTORIC_UK_FHL_PROPERTY_PERIOD_SUMMARY)

  def retrieveHistoricFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino, periodId), method = GET, rel = SELF)

  def listHistoricFhlUkPiePeriodSummaries(appConfig: AppConfig, nino: String): Link =
    Link(href = ukHistoricFhlPiePeriodSummaryUri(appConfig, nino), method = GET, rel = LIST_HISTORIC_UK_FHL_PROPERTY_PERIOD_SUMMARIES)

  // Non-FHL:
  def retrieveHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, periodId), method = GET, rel = SELF)

  def amendHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino, periodId), method = PUT, rel = AMEND_HISTORIC_NON_FHL_UK_PIE_PERIOD_SUMMARY)

  def listHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String): Link =
    Link(href = ukHistoricNonFhlPiePeriodSummaryUri(appConfig, nino), method = GET, rel = LIST_HISTORIC_NON_FHL_UK_PIE_PERIOD_SUMMARIES)

  // Generic
  def listPropertyPeriodSummaries(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) SELF else LIST_PROPERTY_PERIOD_SUMMARIES
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$taxYear", method = GET, rel)
  }

  def deletePropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/annual/$taxYear", method = DELETE, rel = DELETE_PROPERTY_ANNUAL_SUBMISSION)
}
