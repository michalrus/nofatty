package com.michalrus.nofatty.data

import java.util.UUID

import org.joda.time.DateTime

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.lifted.Tag

object DB {

  val DBFile: String = {
    val home = System.getProperty("user.home")
    val dir = s"$home/.nofatty"
    val _ = new java.io.File(dir).mkdirs()
    s"$dir/data.db"
  }

  def db = Database.forURL(s"jdbc:sqlite:$DBFile", driver = "org.sqlite.JDBC")

  val basicProducts = TableQuery[BasicProducts]
  val ingredients = TableQuery[Ingredients]
  val compoundProducts = TableQuery[CompoundProducts]

  if (!new java.io.File(DBFile).exists)
    db withSession { implicit session ⇒
      (basicProducts.ddl ++ ingredients.ddl ++ compoundProducts.ddl).create
    }

  implicit lazy val dateTimeColumnType = MappedColumnType.base[DateTime, Long](_.getMillis, new DateTime(_))

  final class BasicProducts(tag: Tag) extends Table[BasicProduct](tag, "basic_products") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)
    def lastModified = column[DateTime]("lastModified")
    def name = column[String]("name")
    def kcal = column[Double]("kcal")
    def protein = column[Double]("protein")
    def fat = column[Double]("fat")
    def carbohydrate = column[Double]("carbohydrate")
    def fiber = column[Double]("fiber")
    override def * = (uuid, lastModified, name, kcal, protein, fat, carbohydrate, fiber) <> (BasicProduct.tupled, BasicProduct.unapply)
  }

  final class Ingredients(tag: Tag) extends Table[(UUID, UUID, Double)](tag, "ingredients") {
    def compoundProductID = column[UUID]("compound_product")
    def basicProductID = column[UUID]("basic_product")
    def grams = column[Double]("grams")
    override def * = (compoundProductID, basicProductID, grams)
    def pk = primaryKey("ingredients_pk", (compoundProductID, basicProductID))
  }

  final class CompoundProducts(tag: Tag) extends Table[(UUID, DateTime, String, Double)](tag, "compound_products") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)
    def lastModified = column[DateTime]("lastModified")
    def name = column[String]("name")
    def massReduction = column[Double]("mass_reduction")
    override def * = (uuid, lastModified, name, massReduction)
  }

}
