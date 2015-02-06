package com.michalrus.nofatty.ui

import java.awt.{ Dimension, BorderLayout }
import javax.swing.{ JButton, JPanel }

import com.toedter.calendar.JDateChooser

class DateInput extends JPanel {

  private[this] val prev = new JButton("«")
  private[this] val next = new JButton("»")
  private[this] val date = new JDateChooser(new java.util.Date)

  setOpaque(false)
  setLayout(new BorderLayout)
  prev.setMinimumSize(new Dimension(20, 0))
  next.setMinimumSize(new Dimension(20, 0))
  add(prev, BorderLayout.LINE_START)
  add(date, BorderLayout.CENTER)
  add(next, BorderLayout.LINE_END)

}
