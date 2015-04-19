package com.michalrus.nofatty.chart

import java.awt.Color
import java.awt.geom.Ellipse2D
import java.text.{ NumberFormat, SimpleDateFormat }

import com.michalrus.nofatty.data.Day
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer
import org.jfree.data.time.TimeTableXYDataset
import org.joda.time.{ DateTimeZone, LocalDate }

object Chart {
  val Energy = "Energy [kcal]"
  val EnergyTrend = "Energy trend [kcal]"
  val Weight = "Weight [kg]"
  val WeightTrend = "Weight trend [kg]"
  val Mass = "Mass [g]"
  val Percent = "%"
  val Protein = "Protein"
  val Fat = "Fat"
  val Carbohydrate = "Carbohydrate"
  val Fiber = "Fiber"

  val Red = new Color(0xFF, 0, 0, 0x7F)
  val Blue = new Color(0, 0, 0xFF, 0x7F)
  val Green = new Color(0, 0x7F, 0, 0x7F)
  val Yellow = new Color(0xFF, 0xFF, 0, 0x7F)
  val Black = new Color(0, 0, 0, 0x7F)

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
  def refresh(days: Seq[(LocalDate, Option[Day])]): Unit

  import scala.language.implicitConversions
  implicit def localDateToDay(d: LocalDate): org.jfree.data.time.Day =
    new org.jfree.data.time.Day(d.getDayOfMonth, d.getMonthOfYear, d.getYear)
  def datasetToVector(ds: TimeTableXYDataset, series: Int): Vector[(LocalDate, Double)] =
    (0 until ds.getItemCount(series)).map(i ⇒
      (new LocalDate(ds.getXValue(series, i).toLong, DateTimeZone.getDefault), ds.getYValue(series, i))).toVector
}
