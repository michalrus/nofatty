package com.michalrus.nofatty.ui.utils

import java.awt.event.{ FocusEvent, FocusListener, KeyEvent }
import javax.swing.table.TableModel
import javax.swing.{ JTextField, JComponent, JTable, KeyStroke }

class BetterTable(model: TableModel, isInstantlyEditable: (Int, Int) ⇒ Boolean) extends JTable(model) {

  setCellSelectionEnabled(true)
  setSurrendersFocusOnKeystroke(true)
  putClientProperty("terminateEditOnFocusLost", true)
  putClientProperty("JTable.autoStartsEdit", false)

  val _ = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
    put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "selectNextColumnCell")

  lazy val PassThrough: Set[KeyStroke] = {
    import JComponent._
    Set(WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, WHEN_IN_FOCUSED_WINDOW)
      .flatMap(c ⇒ Option(getInputMap(c)).toSet)
      .flatMap(im ⇒ Option(im.allKeys()).toSet)
      .flatMap(_.toSet)
  }

  override def processKeyBinding(ks: KeyStroke, e: KeyEvent, condition: Int, pressed: Boolean): Boolean =
    if (PassThrough contains ks)
      super.processKeyBinding(ks, e, condition, pressed)
    else {
      val leadRow = getSelectionModel.getLeadSelectionIndex
      val leadColumn = getColumnModel.getSelectionModel.getLeadSelectionIndex

      if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        && isFocusOwner
        && ks.getKeyEventType == KeyEvent.KEY_TYPED
        && !ks.getKeyChar.isControl
        && isInstantlyEditable(leadRow, leadColumn)
        && editCellAt(leadRow, leadColumn)) {
        Option(getEditorComponent) match {
          case Some(ec: JTextField with TextFieldUsableAsCellEditor) ⇒
            ec.reset(ks.getKeyChar.toString)
            ec.requestFocus()
            edt {
              ec.setCaretPosition(1)
            }
            true
          case _ ⇒ false
        }
      }
      else false
    }

  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit =
      if (getSelectionModel.isSelectionEmpty) {
        setRowSelectionInterval(0, 0)
        setColumnSelectionInterval(0, 0)
      }
    override def focusLost(e: FocusEvent): Unit = ()
  })

}
