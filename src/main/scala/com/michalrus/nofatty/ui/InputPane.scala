package com.michalrus.nofatty.ui

import java.awt._
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicReference
import javax.swing._
import javax.swing.table.AbstractTableModel

import com.michalrus.nofatty.data._
import com.michalrus.nofatty.ui.utils._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import scala.util.Try

class InputPane extends JPanel {

  def sumEatenProducts(xs: Seq[EatenProduct]): NutritionalValue = {
    val ys = xs flatMap (ep ⇒ Products find ep.product map (p ⇒ p.nutrition * (ep.grams / 100.0)))
    ys.foldLeft(NutritionalValue.Zero)(_ + _)
  }

  def setDate(d: LocalDate): Unit = {
    date.setDate(d)
    day.set(Days find d)
    model.fireTableDataChanged()
    stats.setData(sumEatenProducts(day.get.toSeq flatMap (_.eatenProducts)))
    weight.reset(day.get map (_.weightExpr) getOrElse "")
  }

  val day = new AtomicReference[Option[Day]](None)

  val date = new LocalDateInput(LocalDate.now, setDate)
  val stats = new StatsPane

  val weight = CalculatorTextfield("4.5+1")

  lazy val model = new AbstractTableModel {
    override def getRowCount = 1 + (day.get map (_.eatenProducts.size) getOrElse 0)

    override def getColumnCount = 3

    override def getColumnName(column: Int) = column match {
      case 0 ⇒ "Time"
      case 1 ⇒ "Product"
      case _ ⇒ "Grams"
    }

    override def getColumnClass(columnIndex: Int) = classOf[String]

    override def isCellEditable(rowIndex: Int, columnIndex: Int) = true

    private[this] val formatter = DateTimeFormat.forPattern("HH:mm")

    override def getValueAt(rowIndex: Int, columnIndex: Int): String = {
      if (rowIndex >= getRowCount - 1) ""
      else {
        val eatenProduct = day.get flatMap (_.eatenProducts lift rowIndex)
        val product = eatenProduct flatMap (Products find _.product)
        (eatenProduct, product, columnIndex) match {
          case (Some(ep), _, 0)       ⇒ formatter print ep.time
          case (Some(ep), Some(p), 1) ⇒ p.name
          case (Some(ep), _, 2)       ⇒ ep.gramsExpr
          case _                      ⇒ ""
        }
      }
    }

    override def setValueAt(aValue: AnyRef, rowIndex: Int, columnIndex: Int): Unit = {
      // TODO
    }
  }

  val table: JTable = {
    val t = new JTable(model)
    t.setShowGrid(false)
    t.setCellSelectionEnabled(true)
    t.getTableHeader.setReorderingAllowed(false)
    t.getTableHeader.setResizingAllowed(false)
    t.setRowHeight(30)
    t.putClientProperty("terminateEditOnFocusLost", true)

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

    colProduct.setCellEditor(new AutocompletionCellEditor(Products.names.keySet.toVector.sorted))

    colGrams.setCellEditor(new CalculatorCellEditor)
    colGrams.setCellRenderer({
      val r = new CalculatorCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })

    t
  }

  setOpaque(false)
  layout()

  edt { weight.requestFocus() }

  setDate(LocalDate.now)

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
