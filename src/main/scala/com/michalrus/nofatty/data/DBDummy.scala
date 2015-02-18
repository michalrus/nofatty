package com.michalrus.nofatty.data

import java.io.File
import java.util.UUID

import com.github.tototoshi.csv.CSVReader
import scala.slick.driver.SQLiteDriver.simple._
import com.michalrus.nofatty.data.DB.{ discard ⇒ d }
import org.joda.time.{ DateTimeZone, DateTime, LocalDate, LocalTime }

object DBDummy {

  def csv(path: String): List[List[String]] = {
    val reader = CSVReader.open(new File(path))
    val r = reader.all()
    reader.close()
    r
  }

  val prod = csv("/home/m/prod.csv") drop 1 map {
    case List(id, name, kcal, prot, fat, carb, fib) ⇒
      (id.toInt, name, NutritionalValue(kcal.toDouble, prot.toDouble, fat.toDouble, carb.toDouble, fib.toDouble))
  }

  val prodUUIDs = prod.map { case (id, _, _) ⇒ id → UUID.randomUUID }.toMap

  val eaten = csv("/home/m/eaten.csv") drop 1 map {
    case List(date, time, id, grams) ⇒
      (LocalDate.parse(date), LocalTime.parse(time), id.toInt, grams.toDouble)
  }

  DB.db withTransaction { implicit session ⇒
    d {
      DB.basicProducts ++= (prod map {
        case (id, name, nv) ⇒
          (prodUUIDs(id), DateTime.now, name, nv.kcal.toString, nv.kcal, nv.protein.toString,
            nv.protein, nv.fat.toString, nv.fat, nv.carbohydrate.toString, nv.carbohydrate, nv.fiber.toString, nv.fiber)
      })
    }

    d { DB.days ++= (eaten.map(_._1).distinct map ((_, DateTime.now, DateTimeZone.getDefault, "", None))) }

    d {
      DB.eatenProducts ++= (eaten map {
        case (date, time, id, grams) ⇒
          (date, time, prodUUIDs(id), grams.toString, grams)
      })
    }
  }

}
