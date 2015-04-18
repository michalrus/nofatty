package com.michalrus.nofatty.ui.utils

import java.awt.{ GridBagConstraints, GridBagLayout }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.{ JLabel, SwingConstants, JSlider, JPanel }

import scala.collection.immutable.NumericRange

class Slider(range: NumericRange[Double], initial: Double, label: String, render: Double ⇒ String,
             onSlide: Double ⇒ Unit, onChange: Double ⇒ Unit) extends JPanel {
  require(range.nonEmpty, "empty Slider#range")

  val realInitial: Int = range.map(d ⇒ (d - initial).abs).zipWithIndex.minBy(_._1)._2
  val realMax = range.size - 1

  private[this] val slider = new JSlider(SwingConstants.HORIZONTAL, 0, realMax, realInitial)
  private[this] val lValue = new JLabel

  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent): Unit = {
      val v = slider.getValue.toDouble / realMax * range.end
      lValue.setText(render(v))
      (if (slider.getValueIsAdjusting) onSlide else onChange)(v)
    }
  })

  lValue.setText(render(initial))

  {
    slider.setOpaque(false)
    setOpaque(false)
    setLayout(new GridBagLayout)
    val c = new GridBagConstraints()
    c.fill = GridBagConstraints.HORIZONTAL
    c.gridx = 0; c.gridy = 0
    c.weightx = 1.0; c.weighty = 0.0
    add(new JLabel(s"$label:"), c)
    c.gridx += 1
    add(lValue, c)
    lValue.setHorizontalAlignment(SwingConstants.RIGHT)
    c.gridy += 1; c.gridx = 0; c.gridwidth = 2
    add(slider, c)
  }

}
