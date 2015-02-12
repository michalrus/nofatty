package com.michalrus.nofatty.ui.utils

import java.awt.Component
import javax.swing.{ JTable, BorderFactory, JTextField, AbstractCellEditor }
import javax.swing.table.TableCellEditor

sealed abstract class WeirdTextFieldCellEditor extends AbstractCellEditor with TableCellEditor { self ⇒
  def textFieldFactory: JTextField with StringVerifier

  private[this] val tf = textFieldFactory

  tf.setBorder(BorderFactory.createEmptyBorder)

  override def getTableCellEditorComponent(table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int): Component = {
    tf.setText(value.toString)
    tf
  }

  override def getCellEditorValue: AnyRef = tf.verify(tf.getText) getOrElse ""

  override def stopCellEditing(): Boolean =
    if (tf.verify(tf.getText).isDefined) {
      fireEditingStopped()
      true
    }
    else false
}

final class VerifyingCellEditor(verify: String ⇒ Option[String]) extends WeirdTextFieldCellEditor { self ⇒
  override def textFieldFactory = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] = self.verify(input)
  }
}

final class AutocompletionCellEditor(completions: ⇒ Vector[String]) extends WeirdTextFieldCellEditor { self ⇒
  override def textFieldFactory = new JTextField with SelectAllOnFocus with Autocompletion {
    override def completions: Vector[String] = self.completions
  }
}
