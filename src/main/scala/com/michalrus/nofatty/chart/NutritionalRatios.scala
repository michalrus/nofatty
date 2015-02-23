package com.michalrus.nofatty.chart

import java.text.SimpleDateFormat

import com.michalrus.nofatty.data.EatenProduct
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.renderer.xy.{ StackedXYBarRenderer, StandardXYBarPainter }
import org.jfree.chart.{ ChartFactory, JFreeChart }
import org.jfree.data.time.TimeTableXYDataset

object NutritionalRatios extends Chart {
  import Chart._

  override val title: String = "Nutritional ratios"

  private[this] val dataset = new TimeTableXYDataset()

  override val chart: JFreeChart = {
    val c = ChartFactory.createXYBarChart("", "", true, "Stacked mass-to-protein ratios", dataset)
    c.getXYPlot.setDomainAxis({
      val a = new DateAxis
      a.setDateFormatOverride(new SimpleDateFormat("d-MMM"))
      a
    })
    c.getXYPlot.setRenderer({
      val r = new StackedXYBarRenderer(0.05)
      r.setBarPainter(new StandardXYBarPainter)
      r.setShadowVisible(false)
      r
    })
    c
  }

  override def refresh(): Unit = {
    lastDays foreach {
      case (date, Some(day)) if day.eatenProducts.nonEmpty ⇒
        val nv = EatenProduct.sum(day.eatenProducts)
        val d = nv.protein
        dataset.add(date, nv.protein / d, Protein)
        dataset.add(date, nv.fat / d, Fat)
        dataset.add(date, nv.carbohydrate / d, Carbohydrate)
        dataset.add(date, nv.fiber / d, Fiber)
      case (date, _) ⇒
        Seq(Protein, Fat, Carbohydrate, Fiber) foreach (n ⇒ dataset.add(date, 0.0, n))
    }
  }

  refresh()
}
