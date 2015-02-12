package com.michalrus.nofatty.ui.utils

import java.awt.Color
import java.awt.event.{ FocusEvent, FocusListener }
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.JTextComponent

import com.michalrus.nofatty.Calculator

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

final class CalculatorTextfield(initial: String) extends JTextField { self ⇒
  def userInput: String = _userInput.get
  def calcValue: String = _calcValue.get

  private[this] val _userInput = new AtomicReference(initial)
  private[this] val _calcValue = new AtomicReference(Calculator(initial).fold(_ ⇒ "", v ⇒ f"$v%.1f"))

  def verify(input: String): Option[String] =
    Calculator(input) match {
      case Right(v) ⇒
        val fmt = f"$v%.1f"
        if (isBeingEditedByUser.get) {
          _userInput.set(input)
          _calcValue.set(fmt)
        }
        Some(fmt)
      case _ ⇒ None
    }

  private[this] val normalBackground = self.getBackground
  self.setInputVerifier(new InputVerifier {
    override def verify(input: JComponent): Boolean = {
      self.verify(self.getText) match {
        case Some(v) ⇒
          self.setBackground(normalBackground)
          true
        case None ⇒
          self.setBackground(Color.PINK)
          false
      }
    }
  })

  private[this] val isBeingEditedByUser = new AtomicBoolean(false)

  self.getDocument.addDocumentListener(new DocumentListener {
    def color(): Unit =
      self.setBackground(if (self.verify(self.getText).isDefined) normalBackground else Color.PINK)
    override def insertUpdate(e: DocumentEvent): Unit = color()
    override def changedUpdate(e: DocumentEvent): Unit = color()
    override def removeUpdate(e: DocumentEvent): Unit = color()
  })

  self.addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      self.setText(userInput)
      isBeingEditedByUser.set(true)
      //      self.selectAll()
    }
    override def focusLost(e: FocusEvent): Unit = {
      isBeingEditedByUser.set(false)
      self.setText(calcValue)
      //      self.select(0, 0)
    }
  })
}
