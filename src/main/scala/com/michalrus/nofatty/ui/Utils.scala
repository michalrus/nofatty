package com.michalrus.nofatty.ui

import java.awt.KeyboardFocusManager
import java.awt.event.{ FocusEvent, FocusListener }
import javax.swing.{ KeyStroke, JComponent }
import javax.swing.text.JTextComponent

trait SelectAllOnFocus { self: JTextComponent ⇒
  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = self.select(0, getText.length)
    override def focusLost(e: FocusEvent): Unit = self.select(0, 0)
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
