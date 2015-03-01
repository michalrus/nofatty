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

  def segments(data: Vector[(LocalDate, Double)]): Vector[Vector[(LocalDate, Double)]] = {
    val days = data map { case (d, _) ⇒ Days.daysBetween(Epoch, d).getDays }
    val map = data.toMap.mapValues(Some(_)).withDefault(_ ⇒ None)
    val start = days.min
    val end = days.max
    segmentsO((start to end).toVector map Epoch.plusDays map (d ⇒ map(d) map (v ⇒ (d, v))))
  }

  def segmentsO[T](data: Vector[Option[T]]): Vector[Vector[T]] = {
    def loop(xs: Vector[Option[T]], acc: Vector[Vector[T]]): Vector[Vector[T]] = {
      val trimmed = xs.dropWhile(_.isEmpty)
      if (trimmed.isEmpty) acc
      else {
        val segment = trimmed.takeWhile(_.isDefined).flatten
        val rest = trimmed.dropWhile(_.isDefined)
        loop(rest, acc :+ segment)
      }
    }
    loop(data, Vector.empty)
  }

  def exponentialMovingAverage(alpha: Double, data: Vector[(LocalDate, Double)]): Vector[Vector[(LocalDate, Double)]] =
    segments(data) map {
      case head +: tail ⇒ // safe, segments will always be non-empty
        tail.scanLeft(head) { case ((_, acc), (date, value)) ⇒ (date, alpha * value + (1 - alpha) * acc) }
    }

}
