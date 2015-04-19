package com.michalrus.nofatty.ui

import java.awt.{ GridLayout, BorderLayout }
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

import com.michalrus.nofatty.data.Prefs
import com.michalrus.nofatty.ui.utils.Slider

class PrefsPane(onChange: ⇒ Unit) extends JPanel {

  def updateMod(pref: Prefs.Pref[Double], mod: Double ⇒ Double): Double ⇒ Unit = v ⇒ { pref.set(mod(v)); onChange }
  def update(pref: Prefs.Pref[Double]) = updateMod(pref, identity)

  val weightSmoothing = new Slider(0.20 to 0.95 by 0.01, 1.0 - Prefs.weightAlpha.get, "Weight trend smoothing",
    d ⇒ f"${d * 100.0}%.0f%%", updateMod(Prefs.weightAlpha, 1.0 - _), updateMod(Prefs.weightAlpha, 1.0 - _))

  val energySmoothing = new Slider(0.20 to 0.95 by 0.01, 1.0 - Prefs.energyAlpha.get, "Energy trend smoothing",
    d ⇒ f"${d * 100.0}%.0f%%", updateMod(Prefs.energyAlpha, 1.0 - _), updateMod(Prefs.energyAlpha, 1.0 - _))

  val energyValueMarker = new Slider(0.0 to 6000.0 by 1.0, Prefs.energyMarker.get, "Energy value marker",
    d ⇒ f"$d%.0f kcal", update(Prefs.energyMarker), update(Prefs.energyMarker))

  setOpaque(false)

  setLayout(new BorderLayout)
  add({
    val p = new JPanel
    p.setOpaque(false)
    p.setLayout(new GridLayout(3, 1))
    Seq(weightSmoothing, energySmoothing, energyValueMarker) foreach { sl ⇒
      sl.setBorder(new EmptyBorder(10, 10, 10, 10))
      p.add(sl)
    }
    p
  }, BorderLayout.PAGE_START)
}
