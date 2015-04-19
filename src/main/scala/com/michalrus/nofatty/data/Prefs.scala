package com.michalrus.nofatty.data

import java.util.concurrent.{ ThreadFactory, TimeUnit, ScheduledFuture, Executors }
import java.util.concurrent.atomic.AtomicReference
import scala.slick.driver.SQLiteDriver.simple._
import scala.util.Try

object Prefs {

  private[this] val doubleReaderWriter: (String ⇒ Option[Double], Double ⇒ String) = (s ⇒ Try(s.toDouble).toOption, _.toString)

  val weightAlpha = new Pref("weight_alpha", doubleReaderWriter, 0.25)
  val energyAlpha = new Pref("energy_alpha", doubleReaderWriter, 0.13)
  val energyMarker = new Pref("energy_marker", doubleReaderWriter, 2000.0)

  private[this] val scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r)
      t.setDaemon(true)
      t
    }
  })

  final class Pref[T](key: String, readerWriter: (String ⇒ Option[T], T ⇒ String), initial: T) {
    def get: T = cache.get
    def set(v: T): Unit = {
      Option(scheduled.get) foreach (_.cancel(false))
      cache.set(v)
      scheduled.set(scheduler.schedule(new Runnable {
        override def run(): Unit = {
          DB.db withSession { implicit session ⇒
            val _ = DB.prefs.insertOrUpdate((key, readerWriter._2(v)))
          }
        }
      }, 500, TimeUnit.MILLISECONDS))
    }

    private[this] val cache: AtomicReference[T] = {
      val fromDb = DB.db withSession { implicit session ⇒
        DB.prefs.filter(_.key === key).map(_.value).firstOption flatMap readerWriter._1
      }
      new AtomicReference(fromDb getOrElse initial)
    }

    private[this] val scheduled = new AtomicReference[ScheduledFuture[_]]
  }

}
