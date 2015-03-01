package com.michalrus.nofatty.data

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import com.michalrus.nofatty.Logging
import com.michalrus.nofatty.data.DB.discard
import org.joda.time.{ DateTime, LocalDate }

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.JdbcBackend

final case class NutritionalValue(kcal: Double, protein: Double, fat: Double, carbohydrate: Double, fiber: Double) {
  def +(that: NutritionalValue): NutritionalValue = NutritionalValue(
    this.kcal + that.kcal, this.protein + that.protein, this.fat + that.fat, this.carbohydrate + that.carbohydrate, this.fiber + that.fiber)
  def *(scale: Double): NutritionalValue = NutritionalValue(
    this.kcal * scale, this.protein * scale, this.fat * scale, this.carbohydrate * scale, this.fiber * scale)
}

object NutritionalValue {
  val Zero = NutritionalValue(0.0, 0.0, 0.0, 0.0, 0.0)
  val PerGrams = 100.0

  def weightedMean(xs: Seq[(NutritionalValue, Double)]): NutritionalValue = {
    val weightSum = xs.map(_._2).sum
    val nutrSum = sum(xs map (x ⇒ x._1 * x._2))
    nutrSum * (1.0 / weightSum)
  }

  def sum(xs: Seq[NutritionalValue]): NutritionalValue = xs.foldLeft(Zero)(_ + _)
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
    else NutritionalValue.weightedMean(xs.toSeq) * (1.0 / massReduction)
  }
  /** Checks for potential cycles in the graph of Products */
  def couldContain(subproduct: UUID): Boolean =
    Products.find(subproduct) match {
      case Some(sub: BasicProduct)    ⇒ true
      case Some(sub: CompoundProduct) ⇒ !(sub ingredientsContain this.uuid)
      case None                       ⇒ false
    }
  def ingredientsContain(subproduct: UUID): Boolean =
    uuid == subproduct ||
      (ingredients contains subproduct) ||
      (ingredients.keySet flatMap Products.find collect { case cp: CompoundProduct ⇒ cp } exists (_ ingredientsContain subproduct))
}

object Products extends Logging {

  private[this] val memo = new AtomicReference[Map[UUID, Product]]({
    timed("loading Products") {
      DB.db withSession { implicit session ⇒
        val basics: Seq[Product] = DB.basicProducts.run map {
          case (uuid, lastMod, name, kcalE, kcal, protE, prot, fatE, fat, carbE, carb, fibE, fib) ⇒
            BasicProduct(uuid, lastMod, name, NutritionalValue(kcal, prot, fat, carb, fib), kcalE, protE, fatE, carbE, fibE)
        }
        val ingredients = DB.ingredients.run.groupBy(_._1)
        val compounds: Seq[Product] = DB.compoundProducts.run map {
          case (uuid, lastMod, name, massRed, massPreE, massPostE) ⇒
            val ings = ingredients.getOrElse(uuid, Seq.empty) map { case (_, subprod, gramsE, grams) ⇒ (subprod, (grams, gramsE)) }
            CompoundProduct(uuid, lastMod, name, massRed, massPreE, massPostE, ings.toMap)
        }
        (basics ++ compounds).map(p ⇒ (p.uuid, p)).toMap
      }
    }
  })

  def names: Map[String, UUID] = memo.get map { case (u, p) ⇒ (p.name, u) }

  def find(uuid: UUID): Option[Product] = memo.get.get(uuid)

  private[this] def uncheckedDelete(uuid: UUID)(implicit session: JdbcBackend.Session): Unit = {
    discard { DB.basicProducts.filter(_.uuid === uuid).delete }
    discard { DB.compoundProducts.filter(_.uuid === uuid).delete }
    discard { DB.ingredients.filter(_.compoundProductID === uuid).delete }
  }

  def commit(p: Product): Unit = {
    DB.db withTransaction { implicit session ⇒
      uncheckedDelete(p.uuid)
      discard {
        p match {
          case p: BasicProduct ⇒
            DB.basicProducts += ((p.uuid, p.lastModified, p.name,
              p.kcalExpr, p.nutrition.kcal, p.proteinExpr, p.nutrition.protein, p.fatExpr, p.nutrition.fat,
              p.carbohydrateExpr, p.nutrition.carbohydrate, p.fiberExpr, p.nutrition.fiber))
          case p: CompoundProduct ⇒
            discard { DB.compoundProducts += ((p.uuid, p.lastModified, p.name, p.massReduction, p.massPreExpr, p.massPostExpr)) }
            discard { DB.ingredients ++= p.ingredients map { case (uuid, (grams, gramsExpr)) ⇒ (p.uuid, uuid, gramsExpr, grams) } }
        }
      }
      memo.set(memo.get + (p.uuid → p))
      invalidateProductsContaining(p.uuid)
    }
  }

  final case class DeleteError(usingProducts: List[String], usingDays: List[LocalDate])
  def delete(uuid: UUID): Either[DeleteError, Unit] = {
    val uprods = memo.get.values collect { case p: CompoundProduct if p.ingredients contains uuid ⇒ p.name }
    val udays = DB.db withSession { implicit session ⇒
      DB.eatenProducts.filter(_.productId === uuid).map(_.date).run
    }
    if (uprods.nonEmpty || udays.nonEmpty)
      Left(DeleteError(uprods.toList, udays.toList))
    else {
      memo.set(memo.get - uuid)
      DB.db withTransaction { implicit session ⇒ uncheckedDelete(uuid) }
      Right(())
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
