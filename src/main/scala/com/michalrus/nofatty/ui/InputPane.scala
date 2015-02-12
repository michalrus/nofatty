package com.michalrus.nofatty.ui

import java.awt._
import java.awt.event.KeyEvent
import javax.swing._

import com.michalrus.nofatty.ui.utils._
import org.joda.time.LocalDate

import scala.util.Try

class InputPane extends JPanel {

  setOpaque(false)

  val date = new LocalDateInput(LocalDate.now, _ ⇒ ())
  val stats = new StatsPane

  val weight = CalculatorTextfield("4.5")

  val table: JTable = {
    val cols: Array[AnyRef] = Array("Time", "Product", "Grams")
    val data: Array[Array[AnyRef]] = Array(
      Array("13:15", "granola", "25.0"),
      Array("15:00", "apple", "301.0"),
      Array("18:20", "chocolate 55%", "36.0")
    )

    val t = new JTable(data, cols)
    t.setShowGrid(false)
    t.setRowSelectionAllowed(false)
    t.setColumnSelectionAllowed(false)
    t.getTableHeader.setReorderingAllowed(false)
    t.getTableHeader.setResizingAllowed(false)
    t.setRowHeight(30)

    val _ = t.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "selectNextColumnCell")

    val colTime = t.getColumnModel.getColumn(0)
    val colProduct = t.getColumnModel.getColumn(1)
    val colGrams = t.getColumnModel.getColumn(2)

    colTime.setMaxWidth(50)
    colGrams.setMaxWidth(60)

    val TimeRegex = """^(\d\d?):?(\d\d)$""".r

    colTime.setCellEditor(new VerifyingCellEditor(input ⇒
      TimeRegex.findFirstMatchIn(input) flatMap (m ⇒ Try((m.group(1).toInt, m.group(2).toInt)).toOption) flatMap {
        case (hh, mm) if 0 <= hh && hh <= 23 && 0 <= mm && mm <= 59 ⇒ Some(f"$hh%02d:$mm%02d")
        case _ ⇒ None
      }
    ))

    colProduct.setCellEditor(new AutocompletionCellEditor(Vector("chocolate 55%", "granola", "apple", "canned pineapple", "egg yolk", "whole eggs", "pumpernickel", "olive oil", "uncooked pasta").sorted))

    colGrams.setCellEditor(new CalculatorCellEditor)
    colGrams.setCellRenderer({
      val r = new CalculatorCellRenderer
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
