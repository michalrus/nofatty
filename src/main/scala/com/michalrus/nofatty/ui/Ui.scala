package com.michalrus.nofatty.ui

import java.awt.{ Color, Dimension }
import javax.swing.{ JDesktopPane, JMenuBar, JFrame, UIManager }

object Ui {

  def initialize(): Unit = {
    edt {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

      val f = new JFrame
      f.setTitle("nofatty")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(new Dimension(950, 700))
      f.setVisible(true)
    }
  }

}
