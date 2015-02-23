package com.michalrus.nofatty.chart

import com.michalrus.nofatty.data.{ Days, Day }
import org.jfree.chart.JFreeChart
import org.joda.time.LocalDate

object Chart {
  val Grams = "Mass [g]"
  val Percent = "%"
  val Protein = "Protein"
  val Fat = "Fat"
  val Carbohydrate = "Carbohydrate"
  val Fiber = "Fiber"

  val LastDays = 100
  def lastDays: Vector[(LocalDate, Option[Day])] = {
    val today = LocalDate.now
    (0 until Chart.LastDays).toVector.reverse map today.minusDays map (d ⇒ (d, Days.find(d))) dropWhile (_._2.isEmpty)
  }
}

trait Chart {

  val title: String
  val chart: JFreeChart
  def refresh(): Unit

  import language.implicitConversions
  implicit def localDateToDay(d: LocalDate): org.jfree.data.time.Day =
    new org.jfree.data.time.Day(d.getDayOfMonth, d.getMonthOfYear, d.getYear)

}
