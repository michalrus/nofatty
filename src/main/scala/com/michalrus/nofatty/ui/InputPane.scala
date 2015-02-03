package com.michalrus.nofatty.ui

import java.awt._
import javax.swing.{ JScrollPane, JTable, JLabel, JPanel }

import com.toedter.calendar.JCalendar

class InputPane extends JPanel {

  setOpaque(false)

  val jc = new JCalendar
  val day = new JLabel("February 2, 2015")
  day.setFont(new Font(day.getFont.getName, Font.PLAIN, (day.getFont.getSize * 1.25).toInt))
  val table = new JTable(Array(Array("13:15": AnyRef, "granola", "25")), Array("Hour": AnyRef, "Product", "Grams"))
  table.setFillsViewportHeight(true)

  setLayout(new GridBagLayout)
  setBackground(Color.RED) // todo: .setOpaque
  val c = new GridBagConstraints
  c.insets = new Insets(5, 5, 5, 5)
  c.weighty = 0.0
  c.weightx = 1.0
  c.gridx = 0
  c.gridy = 0
  c.fill = GridBagConstraints.HORIZONTAL
  add(jc, c)

  c.gridy += 1
  add(day, c)

  c.gridy += 1
  c.insets = new Insets(5, 5, 0, 5)
  add(table.getTableHeader, c)

  c.gridy += 1
  c.insets = new Insets(0, 5, 5, 5)
  c.weighty = 1.0
  c.fill = GridBagConstraints.BOTH
  add(new JScrollPane(table), c)

}
