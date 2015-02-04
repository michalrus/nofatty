package com.michalrus.nofatty.ui

import java.awt.{ Color, Font, GridLayout }
import javax.swing.{ JLabel, JPanel }

class StatsPane extends JPanel {

  def setData(kcal: Double, protein: Double, fat: Double, carbohydrate: Double, fiber: Double): Unit = {
    lKcal setText intFormatter.format(kcal)
    lProtein setText f"$protein%.1f g"
    lFat setText f"$fat%.1f g"
    lCarbohydrate setText f"$carbohydrate%.1f g"
    lFiber setText f"$fiber%.1f g"

    val mass = protein + fat + carbohydrate + fiber

    pProtein setText f"${100.0 * protein / mass}%.1f%%"
    pFat setText f"${100.0 * fat / mass}%.1f%%"
    pCarbohydrate setText f"${100.0 * carbohydrate / mass}%.1f%%"
    pFiber setText f"${100.0 * fiber / mass}%.1f%%"

    val divisor = protein

    rProtein setText f"${protein / divisor}%.2f"
    rFat setText f"${fat / divisor}%.2f"
    rCarbohydrate setText f"${carbohydrate / divisor}%.2f"
    rFiber setText f"${fiber / divisor}%.2f"
  }

  private[this] val lKcal, lProtein, lFat, lCarbohydrate, lFiber = new JLabel
  private[this] val pProtein, pFat, pCarbohydrate, pFiber = new JLabel
  private[this] val rProtein, rFat, rCarbohydrate, rFiber = new JLabel
  private[this] val intFormatter = java.text.NumberFormat.getIntegerInstance

  init()

  private[this] def init(): Unit = {
    setOpaque(false)
    setLayout(new GridLayout(4, 5))

    def title(j: JLabel): JLabel = {
      //      j.setFont(new Font(j.getFont.getName, Font.BOLD, j.getFont.getSize))
      j.setFont(new Font(j.getFont.getName, Font.ITALIC, j.getFont.getSize))
      //      j.setBackground(new Color(0xEE, 0xEE, 0xEE))
      //      j.setOpaque(true)
      j
    }

    Vector(new JLabel("kcal"), new JLabel("prot."), new JLabel("fat"), new JLabel("carb."), new JLabel("fiber")) map title foreach add
    Vector(lKcal, lProtein, lFat, lCarbohydrate, lFiber) foreach add
    Vector(title(new JLabel("%M")), pProtein, pFat, pCarbohydrate, pFiber) foreach add
    Vector(title(new JLabel("÷M")), rProtein, rFat, rCarbohydrate, rFiber) foreach add

    setData(0, 0, 0, 0, 0)
  }

}
