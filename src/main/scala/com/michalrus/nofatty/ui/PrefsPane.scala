package com.michalrus.nofatty.ui

import java.awt.{ GridLayout, BorderLayout }
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

import com.michalrus.nofatty.ui.utils.Slider

class PrefsPane(initialWeight: Double, initialEnergy: Double,
                onWeightSmoothing: Double ⇒ Unit,
                onEnergySmoothing: Double ⇒ Unit) extends JPanel {

  val weightSmoothing = new Slider(0.20 to 0.95 by 0.01, initialWeight, "Weight trend smoothing",
    d ⇒ f"${d * 100.0}%.0f%%", onWeightSmoothing, onWeightSmoothing)

  val energySmoothing = new Slider(0.20 to 0.95 by 0.01, initialEnergy, "Energy trend smoothing",
    d ⇒ f"${d * 100.0}%.0f%%", onEnergySmoothing, onEnergySmoothing)

  setOpaque(false)

  setLayout(new BorderLayout)
  add({
    val p = new JPanel
    p.setOpaque(false)
    p.setLayout(new GridLayout(2, 1))
    Seq(weightSmoothing, energySmoothing) foreach { sl ⇒
      sl.setBorder(new EmptyBorder(10, 10, 10, 10))
      p.add(sl)
    }
    p
  }, BorderLayout.PAGE_START)
}
