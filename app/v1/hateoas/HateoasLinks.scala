/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.hateoas

import config.AppConfig
import v1.models.domain.DesTaxYear
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  //Domain URIs

  private def foreignPropertyUri(appConfig: AppConfig, nino: String, businessId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period"

  private def foreignPropertyAnnualSubmissionUri(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/annual/$taxYear"

  private def foreignPropertySubmissionUri(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$submissionId"

  //API resource links
  def listForeignPropertiesPeriodSummaries(appConfig: AppConfig, nino: String, businessId: String, rel: String = SELF): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId), method = GET, rel = rel)

  def createForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId), method = POST, rel = CREATE_PROPERTY_PERIOD_SUMMARY)

  def amendForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, submissionId), method = PUT, rel = AMEND_PROPERTY_PERIOD_SUMMARY)

  def retrieveForeignPropertyPeriodSummary(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, submissionId), method = GET, rel = SELF)

  def retrieveForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, taxYear), method = GET, rel = SELF)

  def amendForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, taxYear), method = PUT, rel = AMEND_PROPERTY_ANNUAL_SUBMISSION)

  def deleteForeignPropertyAnnualSubmission(appConfig: AppConfig, nino: String, businessId: String, taxYear: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, taxYear), method = PUT, rel = DELETE_PROPERTY_ANNUAL_SUBMISSION)
}
