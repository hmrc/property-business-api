package v2.models.response.retrieveHistoricFhlUkPiePeriodSummary

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class PeriodExpenses (premiseRunningCosts: Option[BigDecimal],
                           repairsAndMaintenance: Option[BigDecimal],
                           financialCosts: Option[BigDecimal],
                           professionalFees: Option[BigDecimal],
                           costOfServices: Option[BigDecimal],
                           other: Option[BigDecimal],
                           consolidatedExpenses: Option[BigDecimal],
                           travelCosts: Option[BigDecimal],
                           rentARoom: Option[Float])

case object PeriodExpenses{
  implicit val writes: OWrites[PeriodExpenses] = Json.writes[PeriodExpenses]

  implicit val reads: Reads[PeriodExpenses] = (
    (JsPath \ "premiseRunningCosts").readNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").readNullable[BigDecimal] and
      (JsPath \ "financialCosts").readNullable[BigDecimal] and
      (JsPath \ "professionalFees").readNullable[BigDecimal] and
      (JsPath \ "costOfServices").readNullable[BigDecimal] and
      (JsPath \ "other").readNullable[BigDecimal] and
      (JsPath \ "consolidatedExpenses").readNullable[BigDecimal] and
      (JsPath \ "travelCosts").readNullable[BigDecimal] and
      (JsPath \ "rentARoom").readNullable[Float]
    ) (PeriodExpenses.apply _)
}