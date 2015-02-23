package com.michalrus.nofatty.chart

import java.text.{ NumberFormat, SimpleDateFormat }

import com.michalrus.nofatty.data.{ Day, Days }
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer
import org.joda.time.LocalDate

object Chart {
  val Energy = "Energy [kcal]"
  val Weight = "Weight [kg]"
  val Mass = "Mass [g]"
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

  def setTimeDomain(p: XYPlot): Unit =
    p.setDomainAxis({
      val a = new DateAxis
      a.setDateFormatOverride(new SimpleDateFormat("d-MMM"))
      a
    })

  def setToolTip(r: AbstractXYItemRenderer): Unit =
    r.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
      new SimpleDateFormat("yyyy/MM/dd"), NumberFormat.getInstance))
}

trait Chart {

  val title: String
  val chart: JFreeChart
  def refresh(): Unit

  import scala.language.implicitConversions
  implicit def localDateToDay(d: LocalDate): org.jfree.data.time.Day =
    new org.jfree.data.time.Day(d.getDayOfMonth, d.getMonthOfYear, d.getYear)

}
