package com.michalrus.nofatty.data

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import scala.slick.driver.SQLiteDriver.simple._
import org.joda.time.DateTime

sealed trait Product {
  def uuid: UUID
  def lastModified: DateTime
  def name: String
}
final case class BasicProduct(uuid: UUID, lastModified: DateTime, name: String, kcal: Double, protein: Double, fat: Double, carbohydrate: Double, fiber: Double) extends Product
final case class CompoundProduct(uuid: UUID, lastModified: DateTime, name: String, massReduction: Double, ingredients: Map[UUID, Double]) extends Product

object Products {

  private[this] val memo = new AtomicReference[Map[UUID, Product]]({
    DB.db withSession { implicit session ⇒
      val basics: Seq[Product] = DB.basicProducts.run
      val compounds: Seq[Product] = DB.compoundProducts.run map {
        case (uuid, lastMod, name, massRed) ⇒
          val ings = DB.ingredients.filter(_.compoundProductID === uuid).run map { case (_, u, g) ⇒ (u, g) }
          CompoundProduct(uuid, lastMod, name, massRed, ings.toMap)
      }
      (basics ++ compounds).map(p ⇒ (p.uuid, p)).toMap
    }
  })

  def all = memo.get

}
