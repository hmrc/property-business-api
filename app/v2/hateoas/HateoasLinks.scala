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
    val rel = if (self) "self" else RETRIEVE_FOREIGN_PROPERTY_ANNUAL_SUBMISSION
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
    val rel = if (self) "self" else RETRIEVE_UK_PROPERTY_PERIOD_SUMMARY
    Link(href = ukPeriodSubmissionUri(appConfig, nino, businessId, taxYear, submissionId), method = GET, rel)
  }

  def createAmendUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = PUT, rel = CREATE_AND_AMEND_UK_PROPERTY_ANNUAL_SUBMISSION)

  def retrieveUkPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) "self" else RETRIEVE_UK_PROPERTY_ANNUAL_SUBMISSION
    Link(href = ukAnnualUri(appConfig, nino, businessId, taxYear), method = GET, rel)
  }

  // Generic
  def listPropertyPeriodSummaries(appConfig: AppConfig, nino: String, businessId: String, taxYear: String, self: Boolean): Link = {
    val rel = if (self) "self" else LIST_PROPERTY_PERIOD_SUMMARIES
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$taxYear", method = GET, rel)
  }

  def deletePropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = s"/${appConfig.apiGatewayContext}/$nino/$businessId/annual/$taxYear", method = DELETE, rel = DELETE_PROPERTY_ANNUAL_SUBMISSION)
}
