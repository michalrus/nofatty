package com.michalrus.nofatty.ui

import javax.swing.SwingUtilities

package object utils {

  def edt(f: ⇒ Unit): Unit = {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }

}
