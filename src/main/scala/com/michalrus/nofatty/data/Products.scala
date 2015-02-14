package com.michalrus.nofatty.data

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.DateTime

final case class NutritionalValue(kcal: Double, protein: Double, fat: Double, carbohydrate: Double, fiber: Double) {
  def +(that: NutritionalValue): NutritionalValue = NutritionalValue(
    this.kcal + that.kcal, this.protein + that.protein, this.fat + that.fat, this.carbohydrate + that.carbohydrate, this.fiber + that.fiber)
  def *(scale: Double): NutritionalValue = NutritionalValue(
    this.kcal * scale, this.protein * scale, this.fat * scale, this.carbohydrate * scale, this.fiber * scale)
}

object NutritionalValue {
  val Zero = NutritionalValue(0.0, 0.0, 0.0, 0.0, 0.0)

  def weightedMean(xs: Seq[(NutritionalValue, Double)]): NutritionalValue = {
    val weightSum = xs.map(_._2).sum
    val nutrSum = xs.map(x ⇒ x._1 * x._2).foldLeft(Zero)(_ + _)
    nutrSum * (1.0 / weightSum)
  }
}

sealed trait Product {
  def uuid: UUID
  def lastModified: DateTime
  def name: String
  def nutrition: NutritionalValue
}

final case class BasicProduct(uuid: UUID, lastModified: DateTime, name: String, nutrition: NutritionalValue) extends Product

final case class CompoundProduct(uuid: UUID, lastModified: DateTime, name: String, massReduction: Double, ingredients: Map[UUID, Double]) extends Product {
  lazy val nutrition: NutritionalValue = {
    val xs = ingredients flatMap { case (id, grams) ⇒ Products find id map (p ⇒ (p.nutrition, grams)) }
    NutritionalValue.weightedMean(xs.toSeq) * massReduction
  }
}

object Products {

  private[this] val memo = new AtomicReference[Map[UUID, Product]]({
    DB.db withSession { implicit session ⇒
      val basics: Seq[Product] = DB.basicProducts.run map {
        case (uuid, lastMod, name, kcal, prot, fat, carb, fib) ⇒
          BasicProduct(uuid, lastMod, name, NutritionalValue(kcal, prot, fat, carb, fib))
      }
      val compounds: Seq[Product] = DB.compoundProducts.run map {
        case (uuid, lastMod, name, massRed) ⇒
          val ings = DB.ingredients.filter(_.compoundProductID === uuid).run map { case (_, u, g) ⇒ (u, g) }
          CompoundProduct(uuid, lastMod, name, massRed, ings.toMap)
      }
      (basics ++ compounds).map(p ⇒ (p.uuid, p)).toMap
    }
  })

  def names: Map[String, UUID] = memo.get map { case (u, p) ⇒ (p.name, u) }

  def find(uuid: UUID): Option[Product] = memo.get.get(uuid)

}
