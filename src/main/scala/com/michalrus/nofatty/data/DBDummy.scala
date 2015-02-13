package com.michalrus.nofatty.data

import java.util.UUID

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.DateTime

object DBDummy {

  DB.db withSession { implicit session ⇒
    val butterID, breadID, sandwichID = UUID.randomUUID
    val r1 = DB.basicProducts += BasicProduct(butterID, DateTime.now, "Cow butter", 748, 0, 83, 0, 0)
    val r2 = DB.basicProducts += BasicProduct(breadID, DateTime.now, "White bread", 257, 8.5, 1.4, 54.3, 2.7)
    val r3 = DB.compoundProducts += ((sandwichID, DateTime.now, "Sandwich", 1.0))
    val r4 = DB.ingredients += ((sandwichID, breadID, 25))
    val r5 = DB.ingredients += ((sandwichID, butterID, 5))
    val _ = (r1, r2, r3, r4, r5)
  }

}
