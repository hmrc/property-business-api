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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations.{BusinessIdValidation, JsonFormatValidation, NinoValidation}
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}

class CreateForeignPropertyIncomeAndExpenditurePeriodSummaryValidator extends Validator[RawData???] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldFormatValidation, dateRangeValidation)

  private def parameterFormatValidation: RawData??? => List[List[MtdError]] = (data: RawData???) => {
    List(
      NinoValidation.validate(data.nino),
      BusinessIdValidation.validate(data.businessId)
    )
  }

  private def bodyFormatValidation: RawData??? => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[Body???](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldFormatValidation: RawData??? => List[List[MtdError]] = { data =>
    val body = data.body.as[Body???]

    List(flattenErrors(
      List(
        body.seafarers.map(_.zipWithIndex.flatMap {
          case (item, i) => validateSeafarers(item, i)
        }),
        body.vctSubscription.map(_.zipWithIndex.flatMap {
          case (item, i) => validateVctSubscription(item, i)
        }),
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateSeafarers(seafarers: Seafarers, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = seafarers.customerReference,
        path = s"/seafarers/$arrayIndex/customerReference"
      ),
      AmountValidation.validate(
        field = seafarers.amountDeducted,
        path = s"/seafarers/$arrayIndex/amountDeducted"
      ),
      NameOfShipValidation.validate(
        field = seafarers.nameOfShip,
        path = s"/seafarers/$arrayIndex/nameOfShip"
      ),
      DateValidation.validate(
        field = seafarers.fromDate,
        path = s"/seafarers/$arrayIndex/fromDate"
      ),
      DateValidation.validate(
        field = seafarers.toDate,
        path = s"/seafarers/$arrayIndex/toDate"
      )
    ).flatten
  }

  private def dateRangeValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendOtherDeductionsBody]

    List(flattenErrors(
      List(
        body.seafarers.map(_.zipWithIndex.flatMap {
          case (item, i) => validateToDateBeforeFromDate(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateToDateBeforeFromDate(seafarers: Seafarers, arrayIndex: Int): List[MtdError] = {
    List(
      ToDateBeforeFromDateValidation.validate(
        from = seafarers.fromDate,
        to = seafarers.toDate,
        fromPath = s"/seafarers/$arrayIndex/fromDate",
        toPath = s"/seafarers/$arrayIndex/toDate"
      )
    ).flatten
  }

  override def validate(data: AmendOtherDeductionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
