
package v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import utils.EmptinessChecker
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestBody
import play.api.libs.functional.syntax._

case class Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody(fromDate: String,
                                                                        toDate: String,
                                                                        foreignProperty: Option[Seq[ForeignProperty]])
  extends CreateForeignPropertyPeriodCumulativeSummaryRequestBody

object Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] = EmptinessChecker.use { body =>
    "foreignProperty" -> body.foreignProperty :: HNil
  }

  implicit val reads: Reads[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] =
    Json.reads[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "foreignProperty").writeNullable[Seq[ForeignProperty]]
    )(unlift(Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody.unapply))

}
