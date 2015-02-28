package com.michalrus.nofatty.chart

import org.joda.time.{ Days, LocalDate }

object Trend {

  private[this] val Epoch = new LocalDate(1970, 1, 1)

  def spline1(data: Vector[(LocalDate, Double)]): Vector[(LocalDate, Double)] = {
    if (data.size < 2) data
    else {
      val sorted = data map { case (date, v) ⇒ (date, Days.daysBetween(Epoch, date).getDays, v) } sortBy (_._2)
      sorted.sliding(2).toVector.map {
        case Vector(start, end) ⇒
          (start._2 until end._2).toVector map { days ⇒
            (Epoch.plusDays(days),
              start._3 + ((days.toDouble - start._2.toDouble) / (end._2.toDouble - start._2.toDouble)) * (end._3 - start._3))
          }
      }.flatten ++ data.tail
    }
  }

  def exponentialMovingAverage(alpha: Double, data: Vector[(LocalDate, Double)]): Vector[(LocalDate, Double)] = {
    require(alpha > 0.0 && alpha < 1.0, "<alpha> has to be in (0.0, 1.0)")
    if (data.isEmpty) Vector.empty
    else {
      val normD = {
        val xs = data map { case (date, v) ⇒ (date, Days.daysBetween(Epoch, date).getDays, v) }
        val today = xs.map(_._2).max
        xs map { case (date, day, v) ⇒ (date, today - day, v) }
      }
      val today = normD.minBy(_._2)._1

      def coeff(exponent: Int) = math.pow(1 - alpha, exponent.toDouble)

      (normD map (_._2)) map { emaDay ⇒
        val filtered = normD filter (_._2 >= emaDay)
        val numer = filtered map { case (_, day, v) ⇒ coeff(day - emaDay) * v }
        val denom = filtered map { case (_, day, _) ⇒ coeff(day - emaDay) }
        (today.minusDays(emaDay), numer.sum / denom.sum)
      }
    }
  }

}
