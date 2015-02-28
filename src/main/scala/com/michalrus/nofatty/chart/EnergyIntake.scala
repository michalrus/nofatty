package com.michalrus.nofatty.chart

import java.awt.BasicStroke

import com.michalrus.nofatty.data.{ Day, EatenProduct }
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.{ DatasetRenderingOrder, XYPlot }
import org.jfree.chart.renderer.xy.{ XYSplineRenderer, StandardXYBarPainter, XYBarRenderer, XYLineAndShapeRenderer }
import org.jfree.chart.{ JFreeChart, StandardChartTheme }
import org.jfree.data.time.TimeTableXYDataset
import org.joda.time.LocalDate

object EnergyIntake extends Chart {
  import com.michalrus.nofatty.chart.Chart._

  override val title: String = "Energy intake"

  private[this] val energyDataset, energyTrendDataset, weightDataset, weightTrendDataset = new TimeTableXYDataset()

  override val chart: JFreeChart = {
    val plot = new XYPlot
    val c = new JFreeChart(plot)
    new StandardChartTheme("JFree").apply(c)

    setTimeDomain(plot)

    plot.setRangeAxis(0, new NumberAxis(Energy))
    plot.setRangeAxis(1, {
      val a = new NumberAxis(Weight)
      a.setAutoRangeIncludesZero(false)
      a
    })

    plot.setDataset(0, energyDataset)
    plot.setRenderer(0, {
      val r = new XYBarRenderer(0.05)
      r.setSeriesPaint(0, Blue)
      r.setBarPainter(new StandardXYBarPainter)
      r.setShadowVisible(false)
      setToolTip(r)
      r
    })
    plot.mapDatasetToRangeAxis(0, 0)

    plot.setDataset(1, energyTrendDataset)
    plot.setRenderer(1, {
      val r = new XYSplineRenderer()
      r.setSeriesPaint(0, Blue)
      r.setSeriesShapesVisible(0, false)
      r.setSeriesStroke(0, new BasicStroke(2))
      setToolTip(r)
      r
    })
    plot.mapDatasetToRangeAxis(1, 0)

    plot.setDataset(2, weightDataset)
    plot.setRenderer(2, {
      val r = new XYLineAndShapeRenderer(false, true)
      r.setSeriesPaint(0, Red)
      r.setSeriesShape(0, ellipse(3))
      setToolTip(r)
      r
    })
    plot.mapDatasetToRangeAxis(2, 1)

    plot.setDataset(3, weightTrendDataset)
    plot.setRenderer(3, {
      val r = new XYSplineRenderer()
      r.setSeriesPaint(0, Red)
      r.setSeriesShapesVisible(0, false)
      r.setSeriesStroke(0, new BasicStroke(2))
      setToolTip(r)
      r
    })
    plot.mapDatasetToRangeAxis(3, 1)

    plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE)

    c
  }

  override def refresh(days: Seq[(LocalDate, Option[Day])]): Unit = {
    days foreach {
      case (date, day) ⇒
        energyDataset.remove(date, Energy)
        weightDataset.remove(date, Weight)
        day foreach { day ⇒
          val nv = EatenProduct.sum(day.eatenProducts)
          energyDataset.add(date, nv.kcal, Energy)
          day.weight foreach (w ⇒ weightDataset.add(date, w, Weight))
        }
    }

    weightTrendDataset.clear()
    Trend.exponentialMovingAverage(0.25, Trend.spline1(datasetToVector(weightDataset, 0))) foreach {
      case (date, value) ⇒ weightTrendDataset.add(date, value, WeightTrend)
    }

    energyTrendDataset.clear()
    Trend.exponentialMovingAverage(0.1, Trend.spline1(datasetToVector(energyDataset, 0))) foreach {
      case (date, value) ⇒ energyTrendDataset.add(date, value, EnergyTrend)
    }
  }
}
