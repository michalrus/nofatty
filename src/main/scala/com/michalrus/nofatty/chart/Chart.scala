package com.michalrus.nofatty.chart

import java.awt.Color
import java.awt.geom.Ellipse2D
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

  val Red = new Color(0xFF, 0, 0, 0x7F)
  val Blue = new Color(0, 0, 0xFF, 0x7F)
  val Green = new Color(0, 0xFF, 0, 0x7F)
  val Yellow = new Color(0xFF, 0xFF, 0, 0x7F)

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

  def ellipse(diameter: Double) = new Ellipse2D.Double(-diameter / 2.0, -diameter / 2.0, diameter, diameter)
}

trait Chart {

  val title: String
  val chart: JFreeChart
  def refresh(): Unit

  import scala.language.implicitConversions
  implicit def localDateToDay(d: LocalDate): org.jfree.data.time.Day =
    new org.jfree.data.time.Day(d.getDayOfMonth, d.getMonthOfYear, d.getYear)

}
