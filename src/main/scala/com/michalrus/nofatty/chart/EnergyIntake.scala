package com.michalrus.nofatty.chart

import java.awt.geom.Ellipse2D
import java.awt.{ BasicStroke, Color }

import com.michalrus.nofatty.data.EatenProduct
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.{ DatasetRenderingOrder, XYPlot }
import org.jfree.chart.renderer.xy.{ StandardXYBarPainter, XYBarRenderer, XYSplineRenderer }
import org.jfree.chart.{ JFreeChart, StandardChartTheme }
import org.jfree.data.time.TimeTableXYDataset

object EnergyIntake extends Chart {
  import com.michalrus.nofatty.chart.Chart._

  override val title: String = "Energy intake"

  private[this] val energyDataset, weightDataset = new TimeTableXYDataset()

  override val chart: JFreeChart = {
    val plot = new XYPlot
    val c = new JFreeChart(plot)
    new StandardChartTheme("JFree").apply(c)

    setTimeDomain(plot)

    plot.setDataset(0, energyDataset)
    plot.setRenderer(0, {
      val r = new XYBarRenderer(0.05)
      r.setSeriesPaint(0, Color.ORANGE.darker.darker)
      r.setBarPainter(new StandardXYBarPainter)
      r.setShadowVisible(false)
      setToolTip(r)
      r
    })
    plot.setRangeAxis(0, new NumberAxis(Energy))
    plot.mapDatasetToRangeAxis(0, 0)

    plot.setDataset(1, weightDataset)
    plot.setRenderer(1, {
      val r = new XYSplineRenderer
      r.setSeriesPaint(0, Color.RED)
      r.setSeriesStroke(0, new BasicStroke(1))
      r.setSeriesShape(0, new Ellipse2D.Double(-1.5, -1.5, 3.0, 3.0))
      setToolTip(r)
      r
    })
    plot.setRangeAxis(1, new NumberAxis(Weight))
    plot.mapDatasetToRangeAxis(1, 1)

    plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE)

    c
  }

  override def refresh(): Unit =
    lastDays foreach {
      case (date, Some(day)) ⇒
        val nv = EatenProduct.sum(day.eatenProducts)
        energyDataset.add(date, nv.kcal, Energy)
        day.weight foreach (w ⇒ weightDataset.add(date, w, Weight))
      case (date, _) ⇒
        energyDataset.add(date, 0.0, Energy)
    }

  refresh()
}
