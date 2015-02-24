package com.michalrus.nofatty.chart

import com.michalrus.nofatty.data.{ EatenProduct, Day }
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.renderer.xy.{ StandardXYBarPainter, ClusteredXYBarRenderer }
import org.jfree.chart.{ StandardChartTheme, JFreeChart }
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.TimeTableXYDataset
import org.joda.time.LocalDate

object FatCarbohydrate extends Chart {
  import Chart._

  override val title: String = "P ÷ (F÷C)"

  private[this] val dataset = new TimeTableXYDataset

  override val chart: JFreeChart = {
    val plot = new XYPlot
    val c = new JFreeChart(plot)
    new StandardChartTheme("JFree").apply(c)

    setTimeDomain(plot)

    plot.setDataset(0, dataset)
    plot.setRenderer(0, {
      val r = new ClusteredXYBarRenderer(0.1, true)
      r.setBarPainter(new StandardXYBarPainter)
      r.setSeriesPaint(0, Blue)
      r.setSeriesPaint(1, Green)
      r.setShadowVisible(false)
      setToolTip(r)
      r
    })
    plot.setRangeAxis(0, new NumberAxis("Protein = 1.0"))
    plot.mapDatasetToRangeAxis(0, 0)

    c
  }

  override def refresh(days: Seq[(LocalDate, Option[Day])]): Unit =
    days foreach {
      case (date, day) ⇒
        Seq(Fat, Carbohydrate) foreach (dataset.remove(date, _))
        day filter (_.eatenProducts.nonEmpty) foreach { day ⇒
          val nv = EatenProduct.sum(day.eatenProducts)
          val d = nv.protein
          dataset.add(date, nv.fat / d, Fat)
          dataset.add(date, nv.carbohydrate / d, Carbohydrate)
        }
    }
}
