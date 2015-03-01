package com.michalrus.nofatty.data

import java.util.UUID

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.{ DateTime, DateTimeZone, LocalDate, LocalTime }

import DB.Types._
import DB.discard

final case class EatenProduct(time: LocalTime, product: UUID, gramsExpr: String, grams: Double)
final case class Day(date: LocalDate, lastModified: DateTime, zone: DateTimeZone, weightExpr: String, weight: Option[Double], eatenProducts: Seq[EatenProduct])

object EatenProduct {
  def sum(xs: Seq[EatenProduct]): NutritionalValue =
    NutritionalValue.sum(xs flatMap (ep ⇒ Products find ep.product map (p ⇒ p.nutrition * (ep.grams / NutritionalValue.PerGrams))))
}

object Days {

  def find(date: LocalDate): Option[Day] = {
    DB.db withSession { implicit session ⇒
      DB.days.filter(_.date === date).firstOption map {
        case (d, lastMod, tz, wexpr, w) ⇒
          val eaten = DB.eatenProducts.filter(_.date === d).sortBy(_.time).run map { case (_, tm, prod, gexpr, g) ⇒ EatenProduct(tm, prod, gexpr, g) }
          Day(d, lastMod, tz, wexpr, w, eaten)
      }
    }
  }

  def between(start: LocalDate, end: LocalDate): Seq[Day] = {
    DB.db withSession { implicit session ⇒
      val days = DB.days.filter(_.date >= start).filter(_.date <= end).run
      val eps = DB.eatenProducts.filter(_.date >= start).filter(_.date <= end).run.groupBy(_._1)
      days map {
        case (date, lastMod, zone, wE, w) ⇒
          Day(date, lastMod, zone, wE, w, eps.getOrElse(date, Seq.empty) map {
            case (_, time, prod, gE, g) ⇒ EatenProduct(time, prod, gE, g)
          })
      }
    }
  }

  def commit(d: Day): Unit = {
    DB.db withTransaction { implicit session ⇒
      discard { DB.days.insertOrUpdate((d.date, d.lastModified, d.zone, d.weightExpr, d.weight)) }
      discard { DB.eatenProducts.filter(_.date === d.date).delete }
      discard { DB.eatenProducts ++= d.eatenProducts map (ep ⇒ (d.date, ep.time, ep.product, ep.gramsExpr, ep.grams)) }
    }
  }

  def usingProduct(uuid: UUID): Seq[LocalDate] = {
    DB.db withSession { implicit session ⇒
      DB.eatenProducts.filter(_.productId === uuid).map(_.date).groupBy(identity).map(_._1).run
    }
  }

}
