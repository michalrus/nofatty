package com.michalrus.nofatty.ui

import java.awt._
import javax.swing._

import com.toedter.calendar.JDateChooser

import scala.util.Try

class InputPane extends JPanel {

  setOpaque(false)

  val prevDay = new JButton("«")
  val nextDay = new JButton("»")
  val date = new JDateChooser(new java.util.Date)
  val table = new JTable(Array(Array("13:15": AnyRef, "granola", "25")), Array("Hour": AnyRef, "Product", "Grams")) with NormalTabAction
  val weight = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] =
      if (input.trim.isEmpty) Some("")
      else Try(input.trim.replace(',', '.').toDouble).toOption filterNot (_ < 0.0) map (v ⇒ f"$v%.2f")
  }
  val stats = new StatsPane

  val TimeRegex = """^(\d\d?):(\d\d)$""".r
  val time = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] = {
      if (input.trim.isEmpty) Some("")
      else TimeRegex.findFirstMatchIn(input) flatMap (m ⇒ Try((m.group(1).toInt, m.group(2).toInt)).toOption) flatMap {
        case (hh, mm) if 0 <= hh && hh <= 23 && 0 <= mm && mm <= 59 ⇒ Some(f"$hh%02d:$mm%02d")
        case _ ⇒ None
      }
    }
  }

  val grams = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] =
      if (input.trim.isEmpty) Some("")
      else Try(input.trim.replace(',', '.').toDouble).toOption filterNot (_ < 0.0) map (v ⇒ f"$v%.2f")
  }

  val product = new JTextField with SelectAllOnFocus

  val normalJTextFieldBackground = weight.getBackground

  stats.setData(1530, 130.1, 40.7, 10.2, 10.8)

  layout()

  edt { weight.requestFocus() }

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
    add(date, c)

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

    c.insets = new Insets(5, 5, 5, 5)
    c.gridy += 1
    add(insertLayout(), c)

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

  private[this] def insertLayout(): JPanel = {
    val pane = new JPanel
    pane.setOpaque(false)
    pane.setLayout(new BorderLayout)

    edt {
      val sz = new Dimension(time.getHeight * 2, 0)
      time.setPreferredSize(sz)
      grams.setPreferredSize(sz)
    }

    pane.add(time, BorderLayout.LINE_START)
    pane.add(product, BorderLayout.CENTER)
    pane.add(grams, BorderLayout.LINE_END)

    pane
  }

}
