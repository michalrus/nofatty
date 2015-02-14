package com.michalrus.nofatty.data

import java.util.UUID

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.{ LocalTime, DateTimeZone, LocalDate, DateTime }

object DBDummy {

  DB.db withSession { implicit session ⇒
    @inline def d[F](b: ⇒ F): Unit = { val _ = b; () } // discard non-unit value

    val butterID, breadID, sandwichID = UUID.randomUUID
    d { DB.basicProducts += BasicProduct(butterID, DateTime.now, "Cow butter", 748, 0, 83, 0, 0) }
    d { DB.basicProducts += BasicProduct(breadID, DateTime.now, "White bread", 257, 8.5, 1.4, 54.3, 2.7) }
    d { DB.compoundProducts += ((sandwichID, DateTime.now, "Sandwich", 1.0)) }
    d { DB.ingredients += ((sandwichID, breadID, 25)) }
    d { DB.ingredients += ((sandwichID, butterID, 5)) }

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
