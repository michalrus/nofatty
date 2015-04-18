package com.michalrus.nofatty.ui

import java.awt.{ BorderLayout, Dimension }
import javax.swing._

import com.michalrus.nofatty.Logging
import com.michalrus.nofatty.chart.Chart
import com.michalrus.nofatty.data.{ Products, Days }
import com.michalrus.nofatty.ui.utils._
import org.jfree.chart.ChartPanel
import org.joda.time.LocalDate

object Ui extends Logging {

  val ChartDays = 100

  def initialize(): Unit = edt {
    timed("initializing the UI") {
      val f = timed("creating the frame") {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

        { val _ = UIManager.getLookAndFeelDefaults.put("Slider.paintValue", false) }
        { val _ = UIManager.put("Slider.paintValue", false) }

        val f = new JFrame
        f.setTitle("nofatty")
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        f.setSize(new Dimension(950, 700))
        f.setMinimumSize(f.getSize)
        f.setLayout(new BorderLayout)
        f.setLocationRelativeTo(Unsafe.NullComponent)

        f
      }

      val ltv = timed("creating the left tabbed view") {
        val ltv = new JTabbedPane()
        f.add(ltv, BorderLayout.LINE_START)
        ltv
      }

      val today = timed("loading joda-time") { LocalDate.now }

      val _ = Products

      val charts = {
        import com.michalrus.nofatty.chart._
        val cs = timed("creating charts") { List(EnergyIntake, StackedRatios, FatCarbohydrate) }
        timed(s"loading last $ChartDays days into plots") {
          val days = Days.between(today minusDays ChartDays, today) map (d ⇒ (d.date, Some(d)))
          cs foreach (_.refresh(days))
        }
        cs
      }

      val inputPane = timed("creating InputPane") {
        val inputPane = new InputPane(date ⇒ charts foreach (_.refresh(Seq((date, Days.find(date))))))
        ltv.addTab("Daily input", inputPane)
        inputPane
      }

      timed("creating ProductListPane") {
        ltv.addTab("Products", new ProductListPane({ editedUuid ⇒
          val days = Days.usingProduct(editedUuid) filter (org.joda.time.Days.daysBetween(_, today).getDays < ChartDays) map (d ⇒ (d, Days find d))
          charts foreach (_.refresh(days))
          inputPane.refresh()
        }))
      }

      timed("creating PrefsPane") {
        ltv.addTab("Prefs", new PrefsPane(
          1.0 - Chart.weightAlpha.get, 1.0 - Chart.energyAlpha.get,
          a ⇒ { Chart.weightAlpha.set(1.0 - a); charts.foreach(_ refresh Nil) },
          a ⇒ { Chart.energyAlpha.set(1.0 - a); charts.foreach(_ refresh Nil) }))
      }

      def rtv(select: Int): JTabbedPane = {
        val r = new JTabbedPane()
        charts foreach (ch ⇒ r.addTab(ch.title, new ChartPanel(ch.chart)))
        r.setSelectedIndex(select)
        r
      }

      val split = timed("creating ChartPanels") {
        val split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rtv(0), rtv(1))
        split.setContinuousLayout(true)
        split.setResizeWeight(0.5)
        f.add(split, BorderLayout.CENTER)
        split
      }

      timed("displaying the frame") {
        f.setVisible(true)
        split.setDividerLocation(0.5)
        ltv.setPreferredSize(new Dimension(360, 0))
      }
    }
  }

}
