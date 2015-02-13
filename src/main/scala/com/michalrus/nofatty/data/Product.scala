package com.michalrus.nofatty.data

import java.util.UUID

import org.joda.time.DateTime

sealed trait Product {
  def uuid: UUID
  def lastModified: DateTime
  def name: String
}
final case class BasicProduct(uuid: UUID, lastModified: DateTime, name: String, kcal: Double, protein: Double, fat: Double, carbohydrate: Double, fiber: Double) extends Product
final case class CompoundProduct(uuid: UUID, lastModified: DateTime, name: String, massReduction: Double, ingredients: Map[UUID, Double])
