package com.michalrus.nofatty.chart

import com.michalrus.nofatty.data.{ Day, EatenProduct }
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.{ StackedXYBarRenderer, StandardXYBarPainter }
import org.jfree.chart.{ JFreeChart, StandardChartTheme }
import org.jfree.data.time.TimeTableXYDataset
import org.joda.time.LocalDate

object NutritionalRatios extends Chart {
  import com.michalrus.nofatty.chart.Chart._

  override val title: String = "Stacked ratios"

  private[this] val dataset = new TimeTableXYDataset()

  override val chart: JFreeChart = {
    val plot = new XYPlot
    val c = new JFreeChart(plot)
    new StandardChartTheme("JFree").apply(c)

    setTimeDomain(plot)

    plot.setDataset(0, dataset)
    plot.setRenderer(0, {
      val r = new StackedXYBarRenderer(0.05)
      r.setBarPainter(new StandardXYBarPainter)
      r.setSeriesPaint(0, Red)
      r.setSeriesPaint(1, Blue)
      r.setSeriesPaint(2, Green)
      r.setSeriesPaint(3, Yellow)
      r.setShadowVisible(false)
      setToolTip(r)
      r
    })
    plot.setRangeAxis(0, new NumberAxis("Stacked mass-to-protein ratios"))
    plot.mapDatasetToRangeAxis(0, 0)

    c
  }

  override def refresh(days: Seq[(LocalDate, Option[Day])]): Unit = {
    days foreach {
      case (date, day) ⇒
        Seq(Protein, Fat, Carbohydrate, Fiber) foreach (dataset.remove(date, _))
        day filter (_.eatenProducts.nonEmpty) foreach { day ⇒
          val nv = EatenProduct.sum(day.eatenProducts)
          val d = nv.protein
          dataset.add(date, nv.protein / d, Protein)
          dataset.add(date, nv.fat / d, Fat)
          dataset.add(date, nv.carbohydrate / d, Carbohydrate)
          dataset.add(date, nv.fiber / d, Fiber)
        }
    }
  }
}
