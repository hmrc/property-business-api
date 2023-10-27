/*
 * Copyright 2023 HM Revenue & Customs
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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import scala.math.Ordered.orderingToOrdered

object ResolveTaxYear extends Resolver[String, TaxYear] with Resolvers {

  private val taxYearFormat = "20[1-9][0-9]-[1-9][0-9]".r

  def resolver(error: Option[MtdError], path: Option[String]): SimpleResolver[String, TaxYear] = value => {
    if (taxYearFormat.matches(value)) {
      val startTaxYearStart: Int = 2
      val startTaxYearEnd: Int   = 4

      val endTaxYearStart: Int = 5
      val endTaxYearEnd: Int   = 7

      val start = value.substring(startTaxYearStart, startTaxYearEnd).toInt
      val end   = value.substring(endTaxYearStart, endTaxYearEnd).toInt

      if (end - start == 1)
        Valid(TaxYear.fromMtd(value))
      else
        Invalid(List(withError(error, RuleTaxYearRangeInvalid, path)))

    } else {
      Invalid(List(withError(error, TaxYearFormatError, path)))
    }
  }

  def apply(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    resolver(error, path)(value)

  def apply(minimumTaxYear: TaxYear, value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] = {
    val resolver = this.resolver(error, path) thenValidate satisfiesMin(minimumTaxYear, RuleTaxYearNotSupportedError)

    resolver(value)
  }

}

object ResolveTysTaxYear extends Resolver[String, TaxYear] with Resolvers {

  def resolver(error: Option[MtdError], path: Option[String]): SimpleResolver[String, TaxYear] =
    ResolveTaxYear.resolver(error, path) thenValidate satisfies(List(InvalidTaxYearParameterError) ++ error)(_ >= TaxYear.tysTaxYear)

  def apply(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    resolver(error, path)(value)

}

object ResolveHistoricTaxYear extends Resolvers {

  def resolver(minimumTaxYear: TaxYear, maximumTaxYear: TaxYear): SimpleResolver[String, TaxYear] =
    ResolveTaxYear.resolver(None, None) thenValidate inRange(minimumTaxYear, maximumTaxYear, RuleHistoricTaxYearNotSupportedError)

  def apply(minimumTaxYear: TaxYear, maximumTaxYear: TaxYear, value: String): Validated[Seq[MtdError], TaxYear] =
    resolver(minimumTaxYear, maximumTaxYear)(value)

}
