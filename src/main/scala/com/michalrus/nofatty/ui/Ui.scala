package com.michalrus.nofatty.ui

import java.awt.{ BorderLayout, Dimension }
import javax.swing._
import com.michalrus.nofatty.ui.utils._

object Ui {

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

      val inputPane = new InputPane
      ltv.addTab("Daily input", inputPane)
      ltv.addTab("Products", new ProductListPane({
        inputPane.refresh()
      }))

      def rtv(select: Int): JTabbedPane = {
        val r = new JTabbedPane()
        r.addTab("Chart A", new ChartPane)
        r.addTab("Chart B", new ChartPane)
        r.addTab("Chart C", new ChartPane)
        r.setSelectedIndex(select)
        r
      }

      val split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rtv(0), rtv(1))
      split.setContinuousLayout(true)
      split.setResizeWeight(0.5)
      f.add(split, BorderLayout.CENTER)

      f.setVisible(true)
      split.setDividerLocation(0.5)
      ltv.setPreferredSize(new Dimension(350, 0))
    }
  }

}
