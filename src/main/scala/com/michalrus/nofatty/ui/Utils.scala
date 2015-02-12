package com.michalrus.nofatty.ui

import java.awt.event.{ FocusEvent, FocusListener }
import java.awt.{ Color, Component }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.table.TableCellEditor
import javax.swing.text.JTextComponent

trait SelectAllOnFocus { self: JTextComponent ⇒
  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = self.select(0, getText.length)
    override def focusLost(e: FocusEvent): Unit = self.select(0, 0)
  })
}

trait StringVerifier { self: JTextComponent ⇒
  def verify(input: String): Option[String]
  private[this] val normalBackground = self.getBackground
  self.setInputVerifier(new InputVerifier {
    override def verify(input: JComponent): Boolean = {
      self.verify(self.getText) match {
        case Some(v) ⇒
          self.setText(v)
          self.setBackground(normalBackground)
          true
        case None ⇒
          self.setBackground(Color.PINK)
          false
      }
    }
  })
  self.getDocument.addDocumentListener(new DocumentListener {
    def color(): Unit =
      self.setBackground(if (self.verify(self.getText).isDefined) normalBackground else Color.PINK)
    override def insertUpdate(e: DocumentEvent): Unit = color()
    override def changedUpdate(e: DocumentEvent): Unit = color()
    override def removeUpdate(e: DocumentEvent): Unit = color()
  })
}

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
