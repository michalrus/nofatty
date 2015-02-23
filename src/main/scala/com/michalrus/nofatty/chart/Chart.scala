package com.michalrus.nofatty.chart

import org.jfree.chart.JFreeChart
import org.jfree.data.time.Day
import org.joda.time.LocalDate

object Chart {
  val LastDays = 100
  val Grams = "Mass [g]"
  val Percent = "%"
  val Protein = "Protein"
  val Fat = "Fat"
  val Carbohydrate = "Carbohydrate"
  val Fiber = "Fiber"
}

trait Chart {

  val title: String
  val chart: JFreeChart
  def refresh(): Unit

  import language.implicitConversions
  implicit def localDateToDay(d: LocalDate): Day =
    new Day(d.getDayOfMonth, d.getMonthOfYear, d.getYear)

}
