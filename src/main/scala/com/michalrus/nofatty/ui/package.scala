package com.michalrus.nofatty

import javax.swing.SwingUtilities

package object ui {

  def edt(f: ⇒ Unit): Unit = {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }

}
