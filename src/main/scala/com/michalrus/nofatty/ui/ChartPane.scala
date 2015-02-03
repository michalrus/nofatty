package com.michalrus.nofatty.ui

import java.awt.BorderLayout
import javax.swing.JPanel

import org.jfree.chart.{ ChartFactory, ChartPanel }
import org.jfree.data.xy.{ XYSeries, XYSeriesCollection }

class ChartPane extends JPanel {

  setOpaque(false)

  val dataset = new XYSeriesCollection()
  val series = new XYSeries("fat")
  series.add(0.0, 0.0)
  series.add(1.0, 1.0)
  series.add(2.0, 0.5)
  dataset.addSeries(series)

  val chart = ChartFactory.createXYLineChart("Some chart", "day", "", dataset)
  setLayout(new BorderLayout)
  add(new ChartPanel(chart), BorderLayout.CENTER)

}
