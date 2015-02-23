package com.michalrus.nofatty.chart

import org.jfree.chart.JFreeChart

trait Chart {

  val title: String
  val chart: JFreeChart
  def refresh(): Unit

}
