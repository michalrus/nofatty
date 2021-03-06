package com.michalrus.nofatty.ui.utils

import java.awt.event._
import java.awt.{ BorderLayout, Color, Dimension }
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.JTextComponent

import com.michalrus.nofatty.ui.DefaultListCellRendererModifier

object FilteringListCellRenderer {
  def apply(hilight: ⇒ String) = new DefaultListCellRendererModifier({ obj ⇒
    val s = s"(?i)${Pattern.quote(hilight)}".r.replaceAllIn(s"$obj", mtch ⇒ s"\u0000${mtch.group(0)}\u0001")
    val e = s.replace("<", "&lt;").replace(">", "&gt;").replace("\u0000", "<font color=purple><b>").replace("\u0001", "</b></font>")
    s"<html>$e</html>"
  })
}

object Autocompletion {
  object A {
    val SelectPreviousAutocompletion = "selectPreviousAutocompletion"
    val SelectNextAutocompletion = "selectNextAutocompletion"
  }
}

trait Autocompletion extends TextFieldUsableAsCellEditor { self: JTextComponent ⇒
  def completions: Vector[String]

  final override def correctedInput: Option[String] = if (completions contains self.getText) Some(self.getText) else None
  final override def reset(value: String) = self.setText(value)

  self.setInputVerifier(new InputVerifier {
    override def verify(input: JComponent): Boolean = completions contains self.getText
  })

  self.getDocument.addDocumentListener(new DocumentListener {
    lazy val model = new AbstractListModel[String] {
      // FIXME: so inefficient :3
      val predicate: String ⇒ Boolean = _.toLowerCase contains self.getText.toLowerCase
      override def getSize: Int = completions count predicate
      override def getElementAt(index: Int): String = (completions filter predicate).lift(index).getOrElse("")
    }
    lazy val list = {
      val l = new JList(model)
      l.setLayoutOrientation(JList.VERTICAL)
      l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
      l.setFixedCellHeight(25)
      l.setVisibleRowCount(-1)
      l.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent): Unit = if (e.getClickCount > 1) acceptSelection()
      })
      l.setCellRenderer(FilteringListCellRenderer(self.getText))
      l
    }
    lazy val popup = {
      val p = new JPanel
      p.setSize(new Dimension(self.getWidth, 150))
      p.setOpaque(true)
      p.setBackground(Color.RED)
      p.setLayout(new BorderLayout)
      p.add(new JScrollPane(list), BorderLayout.CENTER)
      p
    }

    self.addFocusListener(new FocusListener {
      override def focusGained(e: FocusEvent): Unit = ()
      override def focusLost(e: FocusEvent): Unit = popup.setVisible(false)
    })

    def modifySelectionBy(di: Int): Unit = {
      val idx = (list.getSelectedIndex + di) min (model.getSize - 1) max 0
      list.setSelectedIndex(idx)
      list.ensureIndexIsVisible(idx)
    }

    def acceptSelection(): Unit = {
      if (!list.isSelectionEmpty) {
        self.setText(list.getSelectedValue)
        popup.setVisible(false)
      }
    }

    // This has to be done using a KeyListener. An Action would require two VK_ENTER’s
    // when Autocompletion was used as a cell editor. Or how do we easily chain Actions?
    self.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = ()
      override def keyPressed(e: KeyEvent): Unit = {
        if (e.getKeyCode == KeyEvent.VK_ENTER) acceptSelection()
      }
      override def keyReleased(e: KeyEvent): Unit = ()
    })

    import Autocompletion.A

    self.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), A.SelectPreviousAutocompletion)
    self.getActionMap.put(A.SelectPreviousAutocompletion, new AbstractAction() {
      override def actionPerformed(e: ActionEvent): Unit = modifySelectionBy(-1)
    })

    self.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), A.SelectNextAutocompletion)
    self.getActionMap.put(A.SelectNextAutocompletion, new AbstractAction() {
      override def actionPerformed(e: ActionEvent): Unit = modifySelectionBy(+1)
    })

    private[this] lazy val normalBackground = self.getBackground
    def autocomplete(): Unit = {
      val nb = normalBackground // force eval; needed if invalid initially
      self.setBackground(if (completions contains self.getText) nb else Color.PINK)

      Option(self.getRootPane) map (_.getLayeredPane) foreach { lpane ⇒
        val p1 = lpane.getLocationOnScreen
        val p2 = self.getLocationOnScreen
        lpane.add(popup, JLayeredPane.POPUP_LAYER, 0)
        popup.setLocation(p2.x - p1.x + 0, p2.y - p1.y + self.getHeight)
        list.setSelectedIndex(0)
        popup.setVisible(true)
      }
    }

    override def insertUpdate(e: DocumentEvent): Unit = autocomplete()
    override def changedUpdate(e: DocumentEvent): Unit = autocomplete()
    override def removeUpdate(e: DocumentEvent): Unit = autocomplete()
  })

  private[this] val _selectAllOnFocus = new AtomicBoolean(true)
  override def setSelectAllOnFocus(v: Boolean): Unit = _selectAllOnFocus.set(v)

  self.addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = if (_selectAllOnFocus.get) self.selectAll()
    override def focusLost(e: FocusEvent): Unit = self.select(0, 0)
  })
}
