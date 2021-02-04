/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveForeignPropertyAnnualSubmission

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEeaEntry
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty.ForeignPropertyEntry

case class RetrieveForeignPropertyAnnualSubmissionResponse(foreignFhlEea: Option[ForeignFhlEeaEntry], foreignProperty: Option[Seq[ForeignPropertyEntry]])

object RetrieveForeignPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit  val format: OFormat[RetrieveForeignPropertyAnnualSubmissionResponse] = Json.format[RetrieveForeignPropertyAnnualSubmissionResponse]

  implicit object RetrieveAnnualSubmissionLinksFactory extends
    HateoasLinksFactory[RetrieveForeignPropertyAnnualSubmissionResponse, RetrieveForeignPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveForeignPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        retrieveForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        deleteForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear)
      )
    }
  }
}

case class RetrieveForeignPropertyAnnualSubmissionHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData