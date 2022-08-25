/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.domain

sealed trait HistoricPropertyType

object HistoricPropertyType {
  case object NonFhl extends HistoricPropertyType
  case object Fhl    extends HistoricPropertyType
}
