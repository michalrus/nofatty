package com.michalrus.nofatty.ui

import java.awt.{ Color, KeyboardFocusManager }
import java.awt.event.{ FocusEvent, FocusListener }
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.{ InputVerifier, KeyStroke, JComponent }
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

trait NormalTabAction { self: JComponent ⇒

  {
    val forward = new java.util.HashSet(
      self.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS))

    { val _ = forward.add(KeyStroke.getKeyStroke("TAB")) }
    self.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward)
    val backward = new java.util.HashSet(
      self.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS))

    { val _ = backward.add(KeyStroke.getKeyStroke("shift TAB")) }
    self.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward)
  }

}
