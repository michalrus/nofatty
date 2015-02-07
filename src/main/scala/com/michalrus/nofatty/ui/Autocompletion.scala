package com.michalrus.nofatty.ui

import java.awt.{ BorderLayout, Color, Dimension }
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.JTextComponent

trait Autocompletion { self: JTextComponent ⇒
  def completions: Vector[String]
  self.getDocument.addDocumentListener(new DocumentListener {
    lazy val model = {
      val m = new DefaultListModel[String]
      completions foreach m.addElement
      m
    }
    lazy val list = {
      val l = new JList(model)
      l.setLayoutOrientation(JList.VERTICAL)
      l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
      l.setFixedCellHeight(25)
      l.setVisibleRowCount(-1)
      l
    }
    lazy val popup = {
      val p = new JPanel
      p.setSize(new Dimension(self.getWidth * 4 / 4, 150))
      p.setOpaque(true)
      p.setBackground(Color.RED)
      p.setLayout(new BorderLayout)
      p.add(new JScrollPane(list), BorderLayout.CENTER)
      p
    }

    def autocomplete(): Unit = {
      val lpane = self.getRootPane.getLayeredPane

      val p1 = lpane.getLocationOnScreen
      val p2 = self.getLocationOnScreen
      lpane.add(popup, JLayeredPane.POPUP_LAYER, 0)
      popup.setLocation(p2.x - p1.x + 0, p2.y - p1.y + self.getHeight)
    }

    override def insertUpdate(e: DocumentEvent): Unit = autocomplete()
    override def changedUpdate(e: DocumentEvent): Unit = autocomplete()
    override def removeUpdate(e: DocumentEvent): Unit = autocomplete()
  })
}
