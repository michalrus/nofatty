package com.michalrus.nofatty.data

import java.util.UUID

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.{ DateTime, DateTimeZone, LocalDate, LocalTime }

import DB.Types._

final case class EatenProduct(time: LocalTime, product: UUID, gramsExpr: String, grams: Double)
final case class Day(date: LocalDate, lastModified: DateTime, zone: DateTimeZone, weightExpr: String, weight: Option[Double], eatenProducts: Seq[EatenProduct])

object Days {

  def find(date: LocalDate): Option[Day] = {
    DB.db withSession { implicit session ⇒
      DB.days.filter(_.date === date).firstOption map {
        case (d, lastMod, tz, wexpr, w) ⇒
          val eaten = DB.eatenProducts.filter(_.date === d).run map { case (_, tm, prod, gexpr, g) ⇒ EatenProduct(tm, prod, gexpr, g) }
          Day(d, lastMod, tz, wexpr, w, eaten)
      }
    }
  }

}
