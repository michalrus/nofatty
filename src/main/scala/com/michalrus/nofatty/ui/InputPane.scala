package com.michalrus.nofatty.ui

import java.awt._
import javax.swing._

import com.toedter.calendar.{ JDateChooser, JCalendar }

class InputPane extends JPanel {

  setOpaque(false)

  val prevDay = new JButton("«")
  val nextDay = new JButton("»")
  val day = new JDateChooser(new java.util.Date)
  val table = new JTable(Array(Array("13:15": AnyRef, "granola", "25")), Array("Hour": AnyRef, "Product", "Grams"))
  val weight = new JTextField
  val stats = new StatsPane

  stats.setData(1530, 130.1, 40.7, 10.2, 10.8)

  layout()

  private[this] def layout(): Unit = {
    table.setFillsViewportHeight(true)

    setLayout(new GridBagLayout)

    val c = new GridBagConstraints
    c.insets = new Insets(5, 5, 5, 0)
    c.weighty = 0.0
    c.weightx = 0.0
    c.gridx = 0
    c.gridy = 0
    c.fill = GridBagConstraints.HORIZONTAL
    add(prevDay, c)

    c.insets = new Insets(5, 0, 5, 0)
    c.gridx += 1
    c.weightx = 1.0
    add(day, c)

    c.insets = new Insets(5, 0, 5, 5)
    c.gridx += 1
    c.weightx = 0.0
    add(nextDay, c)

    c.insets = new Insets(5, 5, 5, 5)
    c.gridx = 0
    c.gridy += 1
    c.gridwidth = 3
    add(weightLayout(), c)

    c.insets = new Insets(10, 5, 15, 5)
    c.gridy += 1
    add(stats, c)

    c.gridy += 1
    c.insets = new Insets(5, 5, 0, 5)
    add(table.getTableHeader, c)

    c.gridy += 1
    c.insets = new Insets(0, 5, 5, 5)
    c.weighty = 1.0
    c.fill = GridBagConstraints.BOTH
    add(new JScrollPane(table), c)
  }

  private[this] def weightLayout(): JPanel = {
    val pane = new JPanel
    pane.setOpaque(false)
    pane.setLayout(new GridLayout(1, 2))

    { val _ = pane.add(new JLabel("Weight [kg]:")) }
    { val _ = pane.add(weight) }

    pane
  }

}
