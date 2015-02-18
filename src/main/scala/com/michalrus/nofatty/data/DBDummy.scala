package com.michalrus.nofatty.data

import java.util.UUID

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.{ LocalTime, DateTimeZone, LocalDate, DateTime }

import DB.{ discard ⇒ d }

object DBDummy {

  DB.db withTransaction { implicit session ⇒
    val butterID, breadID, sandwichID = UUID.randomUUID
    d { DB.basicProducts += ((butterID, DateTime.now, "Cow butter", "748", 748, "0", 0, "83", 83, "0", 0, "0", 0)) }
    d { DB.basicProducts += ((breadID, DateTime.now, "White bread", "257", 257, "17/2", 8.5, "1.4", 1.4, "54.3", 54.3, "2.7", 2.7)) }
    d { DB.compoundProducts += ((sandwichID, DateTime.now, "Sandwich", 1.0, "", "")) }
    d { DB.ingredients += ((sandwichID, breadID, "2*12.5", 25)) }
    d { DB.ingredients += ((sandwichID, butterID, "5", 5)) }

    val today = LocalDate.now
    val yesterday = today minusDays 1
    val tz = DateTimeZone.getDefault
    d { DB.days += ((yesterday, DateTime.now, tz, "56.5-1.1", Some(55.4))) }
    d { DB.eatenProducts += ((yesterday, new LocalTime(9, 22), breadID, "23+8", 31.0)) }
    d { DB.eatenProducts += ((yesterday, new LocalTime(9, 22), breadID, "22", 22.0)) }
    d { DB.eatenProducts += ((yesterday, new LocalTime(9, 22), butterID, "5+7+5", 17.0)) }
    d { DB.eatenProducts += ((yesterday, new LocalTime(17, 3), breadID, "30+34", 64.0)) }
    d { DB.eatenProducts += ((yesterday, new LocalTime(18, 15), butterID, "11/2", 5.5)) }
    d { DB.days += ((today, DateTime.now, tz, "56.8-2", Some(54.8))) }
    d { DB.eatenProducts += ((today, new LocalTime(11, 15), sandwichID, "70/2", 35)) }
    d { DB.eatenProducts += ((today, new LocalTime(11, 15), butterID, "5", 5)) }
    d { DB.eatenProducts += ((today, new LocalTime(16, 30), sandwichID, "70/2", 35)) }
  }

}
