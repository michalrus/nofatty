package com.michalrus.nofatty.chart

import org.jfree.chart.{ ChartFactory, JFreeChart }
import org.jfree.data.xy.{ XYSeries, XYSeriesCollection }

object NutritionalRatios extends Chart {

  override val title: String = "Nutritional ratios"

  override val chart: JFreeChart = {
    val dataset = new XYSeriesCollection()
    val series = new XYSeries("fat")
    series.add(0.0, 0.0)
    series.add(1.0, 1.0)
    series.add(2.0, 0.5)
    dataset.addSeries(series)
    ChartFactory.createXYLineChart(title, "day", "", dataset)
  }

  override def refresh(): Unit = ???

}
