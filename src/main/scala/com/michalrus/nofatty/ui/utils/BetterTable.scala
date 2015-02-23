package com.michalrus.nofatty.ui.utils

import java.awt.event.KeyEvent
import javax.swing.table.TableModel
import javax.swing.{ JComponent, JTable, KeyStroke }

class BetterTable(model: TableModel, isInstantlyEditable: (Int, Int) ⇒ Boolean) extends JTable(model) {

  setCellSelectionEnabled(true)
  setSurrendersFocusOnKeystroke(true)
  putClientProperty("terminateEditOnFocusLost", true)
  putClientProperty("JTable.autoStartsEdit", true)

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
    else if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
      && isFocusOwner
      && ks.getKeyEventType == KeyEvent.KEY_PRESSED
      && !ks.getKeyChar.isControl
      && isInstantlyEditable(getSelectedRow, getSelectedColumn)) {
      println("*** instant edit!")
      // TODO: start editing here
      false
    }
    else false

}
