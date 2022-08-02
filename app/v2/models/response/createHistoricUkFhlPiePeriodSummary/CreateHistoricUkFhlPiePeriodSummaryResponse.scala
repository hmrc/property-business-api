/*
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

package v2.models.response.createHistoricUkFhlPiePeriodSummary

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v2.hateoas.{HateoasLinks, HateoasLinksFactory}
import v2.models.hateoas.{HateoasData, Link}


case class CreateHistoricUkFhlPiePeriodSummaryResponse(submissionId: String)

object CreateHistoricUkFhlPiePeriodSummaryResponse extends HateoasLinks {
  implicit val format: OFormat[CreateHistoricUkFhlPiePeriodSummaryResponse] = Json.format[CreateHistoricUkFhlPiePeriodSummaryResponse]

  implicit object LinksFactory extends HateoasLinksFactory[CreateHistoricUkFhlPiePeriodSummaryResponse, CreateHistoricUkFhlPiePeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: CreateHistoricUkFhlPiePeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendHistoricFhlUkPiePeriodSubmission(appConfig, nino,periodId),
        retrieveHistoricFhlUkPiePeriodSubmission(appConfig, nino, periodId)
      )
    }
  }
}
 case class CreateHistoricUkFhlPiePeriodSummaryHateoasData(nino: String, periodId: String, submissionId:String) extends HateoasData