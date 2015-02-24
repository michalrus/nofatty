package com.michalrus.nofatty.data

import java.io.File

import com.github.tototoshi.csv.CSVReader
import com.michalrus.nofatty.data.DB.{discard => d}
import org.joda.time.{DateTime, DateTimeZone, LocalDate}

object DBDummy {

  def csv(path: String): List[List[String]] = {
    val reader = CSVReader.open(new File(path))
    val r = reader.all()
    reader.close()
    r
  }

  val weights = csv("/home/m/weight.csv") map {
    case List(t, w) ⇒
      (new LocalDate(t.toLong, DateTimeZone.getDefault), (w.toDouble * 10.0).round.toDouble / 10.0)
  }

  weights foreach {
    case (date, weight) ⇒
      val day = Days.find(date) getOrElse Day(date, DateTime.now, DateTimeZone.getDefault, "", None, Seq.empty)
      val mod = day.copy(lastModified = DateTime.now, weightExpr = weight.toString, weight = Some(weight))
      Days.commit(mod)
  }

}
