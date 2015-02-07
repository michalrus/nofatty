package com.michalrus.nofatty.ui

import java.awt.event._
import java.awt.{ BorderLayout, Color, Dimension }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.JTextComponent

trait Autocompletion extends StringVerifier { self: JTextComponent ⇒
  def completions: Vector[String]

  final override def verify(input: String): Option[String] =
    if (completions contains input) Some(input) else None

  self.getDocument.addDocumentListener(new DocumentListener {
    lazy val model = new AbstractListModel[String] {
      // FIXME: so inefficient :3
      val predicate: String ⇒ Boolean = _.toLowerCase contains self.getText.toLowerCase
      override def getSize: Int = completions count predicate
      override def getElementAt(index: Int): String = (completions filter predicate)(index)
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

    self.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = ()
      override def keyPressed(e: KeyEvent): Unit = {
        import KeyEvent._
        e.getKeyCode match {
          case VK_UP    ⇒ modifySelectionBy(-1)
          case VK_DOWN  ⇒ modifySelectionBy(+1)
          case VK_ENTER ⇒ acceptSelection()
          case _        ⇒
        }
      }
      override def keyReleased(e: KeyEvent): Unit = ()
    })

    def autocomplete(): Unit = {
      val lpane = self.getRootPane.getLayeredPane

      val p1 = lpane.getLocationOnScreen
      val p2 = self.getLocationOnScreen
      lpane.add(popup, JLayeredPane.POPUP_LAYER, 0)
      popup.setLocation(p2.x - p1.x + 0, p2.y - p1.y + self.getHeight)
      list.clearSelection()
      popup.setVisible(true)
    }

    override def insertUpdate(e: DocumentEvent): Unit = autocomplete()
    override def changedUpdate(e: DocumentEvent): Unit = autocomplete()
    override def removeUpdate(e: DocumentEvent): Unit = autocomplete()
  })
}
