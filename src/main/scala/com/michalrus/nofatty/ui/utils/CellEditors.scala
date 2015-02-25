package com.michalrus.nofatty.ui.utils

import java.awt.Component
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing._
import javax.swing.table.{ DefaultTableCellRenderer, TableCellEditor }

import com.michalrus.nofatty.Calculator

trait TextFieldUsableAsCellEditor {
  def correctedInput: Option[String]
  def reset(value: String)
  def setSelectAllOnFocus(v: Boolean)
}

sealed abstract class WeirdTextFieldCellEditor extends AbstractCellEditor with TableCellEditor { self ⇒
  def textFieldFactory: JTextField with TextFieldUsableAsCellEditor

  private[this] val tf = textFieldFactory

  tf.setBorder(BorderFactory.createEmptyBorder)

  override def getTableCellEditorComponent(table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int): Component = {
    tf.reset(value.toString)
    tf
  }

  override def getCellEditorValue: AnyRef = tf.correctedInput getOrElse ""

  override def stopCellEditing(): Boolean =
    if (tf.correctedInput.isDefined) {
      fireEditingStopped()
      true
    }
    else false

  override def isCellEditable(e: EventObject): Boolean =
    e match {
      case e: MouseEvent ⇒ e.getClickCount >= 2
      case _             ⇒ true
    }
}

final class VerifyingCellEditor(verify: String ⇒ Option[String]) extends WeirdTextFieldCellEditor { self ⇒
  override def textFieldFactory = new VerifyingTextField("", verify, false, true)
}

final class AutocompletionCellEditor(completions: ⇒ Vector[String]) extends WeirdTextFieldCellEditor { self ⇒
  override def textFieldFactory = new JTextField with Autocompletion {
    override def completions: Vector[String] = self.completions
  }
}

final class CalculatorCellEditor(acceptable: Double ⇒ Boolean) extends WeirdTextFieldCellEditor { self ⇒
  override def textFieldFactory = new VerifyingTextField("", { input ⇒
    Calculator(input) match {
      case Right(v) if acceptable(v) ⇒ Some(input)
      case _                         ⇒ None
    }
  }, false, true)
}

final class CalculatorCellRenderer extends DefaultTableCellRenderer {
  override def setValue(value: AnyRef): Unit = super.setValue(Calculator(value.toString) match {
    case Right(v) ⇒ f"$v%.1f"
    case _        ⇒ ""
  })
}
