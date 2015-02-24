package com.michalrus.nofatty.ui

import java.awt.{ BorderLayout, Dimension }
import javax.swing._
import com.michalrus.nofatty.data.{ Days, Day }
import com.michalrus.nofatty.ui.utils._
import org.jfree.chart.ChartPanel
import org.joda.time.LocalDate

object Ui {

  val ChartDays = 100

  def initialize(): Unit = {
    edt {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

      val f = new JFrame
      f.setTitle("nofatty")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(new Dimension(950, 750))
      f.setMinimumSize(f.getSize)
      f.setLayout(new BorderLayout)
      f.setLocationRelativeTo(Unsafe.NullComponent)

      val ltv = new JTabbedPane()
      f.add(ltv, BorderLayout.LINE_START)

      val charts = {
        import com.michalrus.nofatty.chart._
        val cs = List(EnergyIntake, StackedRatios, FatCarbohydrate)
        val days = chartDays(ChartDays)
        cs foreach (_.refresh(days))
        cs
      }

      val inputPane = new InputPane(date ⇒ charts foreach (_.refresh(Seq((date, Days.find(date))))))
      ltv.addTab("Daily input", inputPane)
      ltv.addTab("Products", new ProductListPane({
        val days = chartDays(ChartDays)
        charts foreach (_.refresh(days))
        inputPane.refresh()
      }))

      def rtv(select: Int): JTabbedPane = {
        val r = new JTabbedPane()
        charts foreach (ch ⇒ r.addTab(ch.title, new ChartPanel(ch.chart)))
        r.setSelectedIndex(select)
        r
      }

      val split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rtv(0), rtv(1))
      split.setContinuousLayout(true)
      split.setResizeWeight(0.5)
      f.add(split, BorderLayout.CENTER)

      f.setVisible(true)
      split.setDividerLocation(0.5)
      ltv.setPreferredSize(new Dimension(360, 0))
    }
  }

  def chartDays(n: Int): Vector[(LocalDate, Option[Day])] = {
    val today = LocalDate.now
    (0 until n).toVector.reverse map today.minusDays map (d ⇒ (d, Days.find(d))) dropWhile (_._2.isEmpty)
  }

}
