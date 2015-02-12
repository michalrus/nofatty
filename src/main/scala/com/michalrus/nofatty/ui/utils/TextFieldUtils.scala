package com.michalrus.nofatty.ui.utils

import java.awt.Color
import java.awt.event.{ FocusEvent, FocusListener }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
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
