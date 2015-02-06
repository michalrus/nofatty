package com.michalrus.nofatty.ui

import java.awt._
import javax.swing._
import javax.swing.table.DefaultTableCellRenderer

import org.joda.time.LocalDate

import scala.util.Try

class InputPane extends JPanel {

  setOpaque(false)

  val date = new LocalDateInput(LocalDate.now, _ ⇒ ())
  val stats = new StatsPane

  val weight = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] =
      if (input.trim.isEmpty) Some("")
      else Try(input.trim.replace(',', '.').toDouble).toOption filterNot (_ < 0.0) map (v ⇒ f"$v%.1f")
  }

  val table: JTable = {
    val cols: Array[AnyRef] = Array("Time", "Product", "Grams")
    val data: Array[Array[AnyRef]] = Array(
      Array("13:15", "granola", "25.0"),
      Array("15:00", "apple", "301.0"),
      Array("18:20", "chocolate 55%", "36.0")
    )

    val t = new JTable(data, cols) //with NormalTabAction
    t.setShowGrid(false)
    t.setRowSelectionAllowed(false)
    t.setColumnSelectionAllowed(false)
    t.getTableHeader.setReorderingAllowed(false)
    t.getTableHeader.setResizingAllowed(false)
    t.setRowHeight(30)

    val colTime = t.getColumnModel.getColumn(0)
    //val colProduct = t.getColumnModel.getColumn(1)
    val colGrams = t.getColumnModel.getColumn(2)

    colTime.setMaxWidth(50)
    colGrams.setMaxWidth(60)

    val TimeRegex = """^(\d\d?):(\d\d)$""".r

    colTime.setCellEditor(new VerifyingCellEditor(input ⇒
      TimeRegex.findFirstMatchIn(input) flatMap (m ⇒ Try((m.group(1).toInt, m.group(2).toInt)).toOption) flatMap {
        case (hh, mm) if 0 <= hh && hh <= 23 && 0 <= mm && mm <= 59 ⇒ Some(f"$hh%02d:$mm%02d")
        case _ ⇒ None
      }
    ))

    colGrams.setCellEditor(new VerifyingCellEditor(input ⇒
      Try(input.trim.replace(',', '.').toDouble).toOption filterNot (_ < 0.0) map (v ⇒ f"$v%.1f")
    ))
    colGrams.setCellRenderer({
      val r = new DefaultTableCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })

    t
  }

  stats.setData(1530, 130.1, 40.7, 10.2, 10.8)

  layout()

  edt { weight.requestFocus() }

  private[this] def layout(): Unit = {
    table.setFillsViewportHeight(true)

    setLayout(new GridBagLayout)

    val c = new GridBagConstraints
    c.gridx = 0
    c.gridy = 0
    c.insets = new Insets(5, 5, 5, 5)
    c.weighty = 0.0
    c.weightx = 1.0
    c.fill = GridBagConstraints.HORIZONTAL
    add(date, c)

    c.gridy += 1
    c.gridwidth = 3
    add(weightLayout(), c)

    c.gridy += 1
    c.insets = new Insets(10, 5, 15, 5)
    add(stats, c)

    c.gridy += 1
    c.insets = new Insets(0, 5, 5, 5)
    c.weighty = 1.0
    c.fill = GridBagConstraints.BOTH
    add({
      val sp = new JScrollPane(table)
      sp.setBorder(BorderFactory.createEmptyBorder)
      sp
    }, c)
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
