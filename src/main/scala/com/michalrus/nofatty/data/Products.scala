package com.michalrus.nofatty.data

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.DateTime

import DB.discard

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

final case class BasicProduct(uuid: UUID, lastModified: DateTime, name: String, nutrition: NutritionalValue,
                              kcalExpr: String, proteinExpr: String, fatExpr: String, carbohydrateExpr: String, fiberExpr: String) extends Product

final case class CompoundProduct(uuid: UUID, lastModified: DateTime, name: String, massReduction: Double,
                                 massPreExpr: String, massPostExpr: String, ingredients: Map[UUID, (Double, String)]) extends Product {
  lazy val nutrition: NutritionalValue = {
    val xs = ingredients flatMap { case (id, (grams, gramsExpr)) ⇒ Products find id map (p ⇒ (p.nutrition, grams)) }
    if (xs.isEmpty) NutritionalValue.Zero
    else NutritionalValue.weightedMean(xs.toSeq) * massReduction
  }
}

object Products {

  private[this] val memo = new AtomicReference[Map[UUID, Product]]({
    DB.db withSession { implicit session ⇒
      val basics: Seq[Product] = DB.basicProducts.run map {
        case (uuid, lastMod, name, kcalE, kcal, protE, prot, fatE, fat, carbE, carb, fibE, fib) ⇒
          BasicProduct(uuid, lastMod, name, NutritionalValue(kcal, prot, fat, carb, fib), kcalE, protE, fatE, carbE, fibE)
      }
      val compounds: Seq[Product] = DB.compoundProducts.run map {
        case (uuid, lastMod, name, massRed, massPreE, massPostE) ⇒
          val ings = DB.ingredients.filter(_.compoundProductID === uuid).run map { case (_, subprod, gramsE, grams) ⇒ (subprod, (grams, gramsE)) }
          CompoundProduct(uuid, lastMod, name, massRed, massPreE, massPostE, ings.toMap)
      }
      (basics ++ compounds).map(p ⇒ (p.uuid, p)).toMap
    }
  })

  def names: Map[String, UUID] = memo.get map { case (u, p) ⇒ (p.name, u) }

  def find(uuid: UUID): Option[Product] = memo.get.get(uuid)

  def commit(p: Product): Unit = {
    DB.db withTransaction { implicit session ⇒
      discard { DB.basicProducts.filter(_.uuid === p.uuid).delete }
      discard { DB.compoundProducts.filter(_.uuid === p.uuid).delete }
      discard {
        p match {
          case p: BasicProduct ⇒
            DB.basicProducts += ((p.uuid, p.lastModified, p.name,
              p.kcalExpr, p.nutrition.kcal, p.proteinExpr, p.nutrition.protein, p.fatExpr, p.nutrition.fat,
              p.carbohydrateExpr, p.nutrition.carbohydrate, p.fiberExpr, p.nutrition.fiber))
          case p: CompoundProduct ⇒
            discard { DB.ingredients.filter(_.compoundProductID === p.uuid).delete }
            discard { DB.compoundProducts += ((p.uuid, p.lastModified, p.name, p.massReduction, p.massPreExpr, p.massPostExpr)) }
            discard { DB.ingredients ++= p.ingredients map { case (uuid, (grams, gramsExpr)) ⇒ (p.uuid, uuid, gramsExpr, grams) } }
        }
      }
      memo.set(memo.get + (p.uuid → p))
      invalidateProductsContaining(p.uuid)
    }
  }

  private[this] def invalidateProductsContaining(uuid: UUID): Unit = {
    val directParents = memo.get.values collect {
      case prod: CompoundProduct if prod.ingredients contains uuid ⇒ prod
    }
    directParents foreach (p ⇒ memo.set(memo.get + (p.uuid → p.copy()))) // just copy, to invalidate lazy val CompoundProduct#nutrition
    directParents map (_.uuid) foreach invalidateProductsContaining // invalidate their parents
  }

}
