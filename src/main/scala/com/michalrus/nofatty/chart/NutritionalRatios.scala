package com.michalrus.nofatty.chart

import java.text.SimpleDateFormat

import com.michalrus.nofatty.data.{ Days, EatenProduct }
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.renderer.xy.{ StackedXYBarRenderer, StandardXYBarPainter }
import org.jfree.chart.{ ChartFactory, JFreeChart }
import org.jfree.data.time.TimeTableXYDataset
import org.joda.time.LocalDate

object NutritionalRatios extends Chart {
  import com.michalrus.nofatty.chart.Chart._

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
    val today = LocalDate.now
    val days = (0 until Chart.LastDays).toVector.reverse map today.minusDays map (d ⇒ (d, Days.find(d))) dropWhile (_._2.isEmpty)

    days foreach {
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
