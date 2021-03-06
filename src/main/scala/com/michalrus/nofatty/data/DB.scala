package com.michalrus.nofatty.data

import java.util.UUID

import org.joda.time.{ DateTime, DateTimeZone, LocalDate, LocalTime }

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.lifted.Tag

object DB {

  /** Discards a non-unit value. */
  @inline def discard[F](b: ⇒ F): Unit = { val _ = b; () }

  object Types {
    implicit lazy val dateTimeColumnType = MappedColumnType.base[DateTime, Long](_.getMillis, new DateTime(_))
    implicit lazy val dateTimeZoneColumnType = MappedColumnType.base[DateTimeZone, String](_.getID, DateTimeZone.forID)
    lazy val LocalDateEpoch = new LocalDate(1970, 1, 1)
    implicit lazy val localDateColumnType = MappedColumnType.base[LocalDate, Int](
      org.joda.time.Days.daysBetween(LocalDateEpoch, _).getDays,
      LocalDateEpoch.plusDays)
    implicit lazy val localTimeColumnType = MappedColumnType.base[LocalTime, Int](
      _.getMillisOfDay, ms ⇒ LocalTime.fromMillisOfDay(ms.toLong)
    )
  }

  val DBFile: String = {
    val home = System.getProperty("user.home")
    val dir = s"$home/.nofatty"
    val _ = new java.io.File(dir).mkdirs()
    s"$dir/data.db"
  }

  lazy val db = Database.forURL(s"jdbc:sqlite:$DBFile", driver = "org.sqlite.JDBC")

  val version = db withSession { implicit sess ⇒
    import slick.jdbc.{ StaticQuery ⇒ Q }
    Q.queryNA[Int]("PRAGMA user_version").first
  }

  val basicProducts = TableQuery[BasicProducts]
  val ingredients = TableQuery[Ingredients]
  val compoundProducts = TableQuery[CompoundProducts]
  val days = TableQuery[Days]
  val eatenProducts = TableQuery[EatenProducts]
  val prefs = TableQuery[Prefs]

  db withSession { implicit session ⇒
    def updateV(v: Int): Unit = {
      val _ = slick.jdbc.StaticQuery.updateNA(s"PRAGMA user_version = $v").first
    }

    if (!new java.io.File(DBFile).exists)
      (prefs.ddl ++ basicProducts.ddl ++ ingredients.ddl ++ compoundProducts.ddl ++ days.ddl ++ eatenProducts.ddl).create
    else if (version == 0) {
      prefs.ddl.create
      updateV(1)
    }
  }

  import Types._

  final class Prefs(tag: Tag) extends Table[(String, String)](tag, "prefs") {
    def key = column[String]("key", O.PrimaryKey)
    def value = column[String]("value")
    override def * = (key, value)
  }

  final class BasicProducts(tag: Tag) extends Table[(UUID, DateTime, String, String, Double, String, Double, String, Double, String, Double, String, Double)](tag, "basic_products") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)
    def lastModified = column[DateTime]("lastModified")
    def name = column[String]("name")
    def kcalExpr = column[String]("kcal_expr")
    def kcal = column[Double]("kcal")
    def proteinExpr = column[String]("protein_expr")
    def protein = column[Double]("protein")
    def fatExpr = column[String]("fat_expr")
    def fat = column[Double]("fat")
    def carbohydrateExpr = column[String]("carbohydrate_expr")
    def carbohydrate = column[Double]("carbohydrate")
    def fiberExpr = column[String]("fiber_expr")
    def fiber = column[Double]("fiber")
    override def * = (uuid, lastModified, name, kcalExpr, kcal, proteinExpr, protein, fatExpr, fat, carbohydrateExpr, carbohydrate, fiberExpr, fiber)
  }

  final class Ingredients(tag: Tag) extends Table[(UUID, UUID, String, Double)](tag, "ingredients") {
    def compoundProductID = column[UUID]("compound_product")
    def basicProductID = column[UUID]("basic_product")
    def gramsExpr = column[String]("grams_expr")
    def grams = column[Double]("grams")
    override def * = (compoundProductID, basicProductID, gramsExpr, grams)
    def pk = primaryKey("ingredients_pk", (compoundProductID, basicProductID))
    def compoundProduct = foreignKey("compound_product_fk", compoundProductID, compoundProducts)(_.uuid)
    def basicProduct = foreignKey("basic_product_fk", basicProductID, basicProducts)(_.uuid)
  }

  final class CompoundProducts(tag: Tag) extends Table[(UUID, DateTime, String, Double, String, String)](tag, "compound_products") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)
    def lastModified = column[DateTime]("lastModified")
    def name = column[String]("name")
    def massReduction = column[Double]("mass_reduction")
    def massPreExpr = column[String]("mass_pre_expr")
    def massPostExpr = column[String]("mass_post_expr")
    override def * = (uuid, lastModified, name, massReduction, massPreExpr, massPostExpr)
  }

  final class Days(tag: Tag) extends Table[(LocalDate, DateTime, DateTimeZone, String, Option[Double])](tag, "days") {
    def date = column[LocalDate]("date", O.PrimaryKey)
    def lastModified = column[DateTime]("lastModified")
    def zone = column[DateTimeZone]("zone")
    def weightExpr = column[String]("weight_expr")
    def weight = column[Option[Double]]("weight")
    override def * = (date, lastModified, zone, weightExpr, weight)
  }

  final class EatenProducts(tag: Tag) extends Table[(LocalDate, LocalTime, UUID, String, Double)](tag, "eaten_products") {
    def date = column[LocalDate]("date")
    def time = column[LocalTime]("time")
    def productId = column[UUID]("product")
    def gramsExpr = column[String]("grams_expr")
    def grams = column[Double]("grams")
    override def * = (date, time, productId, gramsExpr, grams)
    def dateTimeIdx = index("date_time_idx", (date, time))
  }

}
