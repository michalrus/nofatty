package com.michalrus.nofatty.ui.utils

import java.awt.Component
import javax.swing.{ JTable, BorderFactory, JTextField, AbstractCellEditor }
import javax.swing.table.{ DefaultTableCellRenderer, TableCellEditor }

import com.michalrus.nofatty.Calculator

trait TextFieldUsableAsCellEditor {
  def correctedInput: Option[String]
  def reset(value: String)
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
