
package v5.createForeignPropertyPeriodCumulativeSummary

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleTaxYearNotSupportedError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import scala.math.Ordered.orderingToOrdered

sealed trait CreateForeignPropertyPeriodCumulativeSummarySchema

object CreateForeignPropertyPeriodCumulativeSummarySchema {
  case object Def1 extends CreateForeignPropertyPeriodCumulativeSummarySchema

  def schemaFor(taxYear: String): Validated[Seq[MtdError], CreateForeignPropertyPeriodCumulativeSummarySchema] =
    taxYear match {
      case taxYearString => ResolveTaxYear(taxYearString) andThen schemaFor
    }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateForeignPropertyPeriodCumulativeSummarySchema] = {
    if (taxYear < TaxYear.starting(2025)) Invalid(Seq(RuleTaxYearNotSupportedError))
    else Valid(Def1)
  }

}
