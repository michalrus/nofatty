package com.michalrus.nofatty.ui

import java.awt._
import java.awt.event.{ FocusEvent, FocusListener, KeyEvent }
import java.util.concurrent.atomic.AtomicReference
import javax.swing._
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.table.AbstractTableModel

import com.michalrus.nofatty.Calculator
import com.michalrus.nofatty.data._
import com.michalrus.nofatty.ui.utils._
import org.joda.time.{ DateTimeZone, DateTime, LocalDate }
import org.joda.time.format.DateTimeFormat

import scala.util.Try

class InputPane extends JPanel {

  def sumEatenProducts(xs: Seq[EatenProduct]): NutritionalValue = {
    val ys = xs flatMap (ep ⇒ Products find ep.product map (p ⇒ p.nutrition * (ep.grams / 100.0)))
    ys.foldLeft(NutritionalValue.Zero)(_ + _)
  }

  private[this] def onDateChanged(d: LocalDate): Unit = {
    day.set(Days find d)
    model.fireTableDataChanged()
    stats.setData(sumEatenProducts(day.get.toSeq flatMap (_.eatenProducts)))
    weight.reset(day.get map (_.weightExpr) getOrElse "")
  }

  val day = new AtomicReference[Option[Day]](None)

  val date = new LocalDateInput(LocalDate.now, onDateChanged)
  val stats = new StatsPane
  val selectionStats = new StatsPane

  val weight = CalculatorTextfield("", _ > 0.0, allowEmpty = true)

  weight.addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = ()
    override def focusLost(e: FocusEvent): Unit = {
      lazy val correctedInput = weight.correctedInput filter (_.nonEmpty) map (_.toDouble)
      day.get match {
        case Some(d) if d.weightExpr != weight.originalInput ⇒
          Days.commit(d.copy(lastModified = DateTime.now, weight = correctedInput, weightExpr = weight.originalInput))
          onDateChanged(date.date)
        case None if weight.originalInput.nonEmpty ⇒
          Days.commit(Day(date.date, DateTime.now, DateTimeZone.getDefault, weight.originalInput, correctedInput, Seq.empty))
          onDateChanged(date.date)
        case _ ⇒
      }
    }
  })

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
      if (rowIndex == getRowCount - 1) {
        // TODO: adding a new record
      }
      else day.get foreach { day ⇒
        val oldEp = day.eatenProducts(rowIndex)
        val newEp: EatenProduct = columnIndex match {
          case 0 ⇒ oldEp.copy(time = formatter.parseLocalTime(aValue.toString))
          case 1 ⇒ oldEp.copy(product = Products.names.getOrElse(aValue.toString, throw new Exception("shouldn’t ever be thrown, really")))
          case _ ⇒ oldEp.copy(gramsExpr = aValue.toString, grams = Calculator(aValue.toString).right getOrElse 0.0)
        }
        val newEps = day.eatenProducts.updated(rowIndex, newEp)
        Days.commit(day.copy(lastModified = DateTime.now, eatenProducts = newEps))
        onDateChanged(day.date)
      }
    }
  }

  val table: JTable = {
    val t = new JTable(model)
    t.setShowGrid(false)
    t.setCellSelectionEnabled(true)
    t.getTableHeader.setReorderingAllowed(false)
    t.getTableHeader.setResizingAllowed(false)
    t.setRowHeight(30)
    t.setSurrendersFocusOnKeystroke(true)
    t.putClientProperty("terminateEditOnFocusLost", true)
    t.putClientProperty("JTable.autoStartsEdit", false)

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

    colGrams.setCellEditor(new CalculatorCellEditor(_ > 0.0))
    colGrams.setCellRenderer({
      val r = new CalculatorCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })

    t.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      override def valueChanged(e: ListSelectionEvent): Unit = {
        val eps = day.get.toSeq flatMap (_.eatenProducts)
        val selected = eps.zipWithIndex filter { case (_, i) ⇒ t.isRowSelected(i) } map { case (ep, _) ⇒ ep }
        selectionStats.setData(sumEatenProducts(selected))
      }
    })

    t
  }

  setOpaque(false)
  layout()

  edt { weight.requestFocus() }

  onDateChanged(date.date)

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
    add(stats, c)

    c.gridy += 1
    c.weighty = 1.0
    c.fill = GridBagConstraints.BOTH
    add({
      val sp = new JScrollPane(table)
      sp.setBorder(BorderFactory.createEmptyBorder)
      sp
    }, c)

    c.gridy += 1
    c.weighty = 0.0
    c.fill = GridBagConstraints.HORIZONTAL
    add(selectionStats, c)
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
