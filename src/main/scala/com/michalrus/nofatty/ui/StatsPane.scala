package com.michalrus.nofatty.ui

import java.awt.{ Color, Font, GridLayout }
import javax.swing.border.{ TitledBorder, EmptyBorder }
import javax.swing.{ BorderFactory, JLabel, JPanel }

import com.michalrus.nofatty.data.NutritionalValue

class StatsPane extends JPanel {

  def setTitle(t: String): Unit = {
    border.setTitle(t)
  }

  def setData(n: NutritionalValue, mass: Double): Unit = {
    lKcal setText intFormatter.format(n.kcal)
    lProtein setText f"${n.protein}%.1f g"
    lFat setText f"${n.fat}%.1f g"
    lCarbohydrate setText f"${n.carbohydrate}%.1f g"
    lFiber setText f"${n.fiber}%.1f g"

    if (mass != 0.0) {
      pProtein setText f"${100.0 * n.protein / mass}%.1f%%"
      pFat setText f"${100.0 * n.fat / mass}%.1f%%"
      pCarbohydrate setText f"${100.0 * n.carbohydrate / mass}%.1f%%"
      pFiber setText f"${100.0 * n.fiber / mass}%.1f%%"
    }
    else Set(pProtein, pFat, pCarbohydrate, pFiber) foreach (_ setText "—")

    val divisor = n.protein

    if (divisor != 0.0) {
      rProtein setText f"${n.protein / divisor}%.2f"
      rFat setText f"${n.fat / divisor}%.2f"
      rCarbohydrate setText f"${n.carbohydrate / divisor}%.2f"
      rFiber setText f"${n.fiber / divisor}%.2f"
    }
    else Set(rProtein, rFat, rCarbohydrate, rFiber) foreach (_ setText "—")
  }

  private[this] val lKcal, lProtein, lFat, lCarbohydrate, lFiber = new JLabel
  private[this] val pProtein, pFat, pCarbohydrate, pFiber = new JLabel
  private[this] val rProtein, rFat, rCarbohydrate, rFiber = new JLabel
  private[this] val intFormatter = java.text.NumberFormat.getIntegerInstance
  private[this] val border = BorderFactory.createTitledBorder("")

  init()

  private[this] def init(): Unit = {
    setOpaque(false)
    setLayout(new GridLayout(4, 5))

    border.setTitleJustification(TitledBorder.CENTER)
    setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(0, 5, 5, 5)))

    def title(j: JLabel): JLabel = {
      j.setFont(new Font(j.getFont.getName, Font.ITALIC, j.getFont.getSize))
      j.setForeground(new Color(0xCC, 0xCC, 0xCC))
      j
    }

    Vector(new JLabel("kcal"), new JLabel("prot."), new JLabel("fat"), new JLabel("carb."), new JLabel("fiber")) map title foreach add
    Vector(lKcal, lProtein, lFat, lCarbohydrate, lFiber) foreach add
    Vector(title(new JLabel("%M")), pProtein, pFat, pCarbohydrate, pFiber) foreach add
    Vector(title(new JLabel("÷M")), rProtein, rFat, rCarbohydrate, rFiber) foreach add

    setData(NutritionalValue.Zero, 0.0)
  }

}
