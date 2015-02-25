package com.michalrus.nofatty.ui.utils

import java.awt.Color
import java.awt.event.{ FocusEvent, FocusListener }
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }

import com.michalrus.nofatty.Calculator

final class VerifyingTextField(initial: String,
                               correct: String ⇒ Option[String],
                               rememberOriginalInput: Boolean,
                               selectAllOnFocus: Boolean) extends JTextField with TextFieldUsableAsCellEditor { self ⇒
  private[this] val _originalInput = new AtomicReference[String]
  private[this] val _correctedInput = new AtomicReference[Option[String]]
  private[this] val isBeingEditedByUser = new AtomicBoolean

  def originalInput: String = _originalInput.get
  override def correctedInput: Option[String] = _correctedInput.get
  override def reset(value: String): Unit = {
    _originalInput.set(value)
    _correctedInput.set(correct(value))
    isBeingEditedByUser.set(false)
    self.setText(correctedInput getOrElse "")
  }

  reset(initial)

  private[this] val normalBackground = self.getBackground

  self.setInputVerifier(new InputVerifier {
    override def verify(input: JComponent): Boolean = correctedInput.isDefined
  })

  self.getDocument.addDocumentListener(new DocumentListener {
    def check(): Unit = {
      val input = self.getText
      correct(input) match {
        case Some(v) ⇒
          _correctedInput.set(Some(v))
          if (isBeingEditedByUser.get) _originalInput.set(if (rememberOriginalInput) input else v)
          self.setBackground(normalBackground)
        case None ⇒
          _correctedInput.set(None)
          self.setBackground(Color.PINK)
      }
    }
    override def insertUpdate(e: DocumentEvent): Unit = check()
    override def changedUpdate(e: DocumentEvent): Unit = check()
    override def removeUpdate(e: DocumentEvent): Unit = check()
  })

  private[this] val _selectAllOnFocus = new AtomicBoolean(selectAllOnFocus)
  override def setSelectAllOnFocus(v: Boolean): Unit = _selectAllOnFocus.set(v)

  self.addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      self.setText(originalInput)
      isBeingEditedByUser.set(true)
      if (_selectAllOnFocus.get) self.selectAll()
    }
    override def focusLost(e: FocusEvent): Unit = {
      isBeingEditedByUser.set(false)
      self.setText(correctedInput getOrElse "")
      self.select(0, 0)
    }
  })
}

object CalculatorTextfield {
  def apply(initial: String, acceptable: Double ⇒ Boolean, allowEmpty: Boolean): VerifyingTextField = new VerifyingTextField(initial, { input ⇒
    if (allowEmpty && input.isEmpty) Some("")
    else Calculator(input) match {
      case Right(v) if acceptable(v) ⇒
        Some(f"$v%.1f")
      case _ ⇒ None
    }
  }, rememberOriginalInput = true, selectAllOnFocus = true)
}
