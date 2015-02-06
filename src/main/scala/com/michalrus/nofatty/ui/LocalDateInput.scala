package com.michalrus.nofatty.ui

import java.awt.event.{ KeyEvent, KeyListener, ActionEvent, ActionListener }
import java.awt.{ Dimension, GridBagConstraints, GridBagLayout }
import java.util.concurrent.atomic.AtomicReference
import javax.swing.{ JButton, JPanel, JTextField }

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import scala.util.Try

class LocalDateInput(initialDate: LocalDate, onChange: LocalDate ⇒ Unit) extends JPanel {

  private[this] val formatter = DateTimeFormat.forPattern("MMMM d, Y")
  private[this] val prev = new JButton("«")
  private[this] val next = new JButton("»")
  private[this] val text = new JTextField with SelectAllOnFocus with StringVerifier {
    override def verify(input: String): Option[String] = {
      val r = Try(formatter.parseLocalDate(input)).toOption filter (d ⇒ d.getYear >= 1000 && d.getYear < 9999)
      r foreach { date ⇒
        if (currentDate.get != date) {
          edt { onChange(date) }
          currentDate.set(date)
        }
      }
      r map formatter.print
    }
  }

  private[this] val currentDate = new AtomicReference[LocalDate](initialDate)
  def date: LocalDate = currentDate.get
  def setDate(d: LocalDate): Unit = {
    text.setText(formatter.print(d))
  }
  private[this] def plusOneDay(): Unit = {
    setDate(currentDate.get.plusDays(1))
    text.requestFocus()
    text.selectAll()
  }
  private[this] def minusOneDay(): Unit = {
    setDate(currentDate.get.minusDays(1))
    text.requestFocus()
    text.selectAll()
  }

  prev.setFocusable(false)
  next.setFocusable(false)
  prev.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = minusOneDay()
  })
  next.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = plusOneDay()
  })

  text.addKeyListener(new KeyListener {
    override def keyTyped(e: KeyEvent): Unit = ()

    override def keyPressed(e: KeyEvent): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_UP   ⇒ plusOneDay()
        case KeyEvent.VK_DOWN ⇒ minusOneDay()
        case _                ⇒
      }
    }

    override def keyReleased(e: KeyEvent): Unit = ()
  })

  setOpaque(false)
  setLayout(new GridBagLayout)
  prev.setPreferredSize(new Dimension(25, 0))
  next.setPreferredSize(new Dimension(25, 0))
  text.setText(formatter.print(initialDate))

  val c = new GridBagConstraints
  c.fill = GridBagConstraints.BOTH
  c.gridx = 0
  c.gridy = 0
  c.weightx = 0.0
  c.weighty = 1.0
  add(prev, c)

  c.gridx += 1; c.weightx = 1.0
  add(text, c)

  c.gridx += 1; c.weightx = 0.0
  add(next, c)

}
