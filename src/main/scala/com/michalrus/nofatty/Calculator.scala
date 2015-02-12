package com.michalrus.nofatty

import org.parboiled.errors.ErrorUtils
import org.parboiled.scala._

object Calculator extends Parser {

  final case class Error(msg: String)

  def apply(expression: String): Either[Error, Double] = {
    val result = ReportingParseRunner(Input).run(expression)
    result.result match {
      case Some(v) ⇒ Right(v)
      case None    ⇒ Left(Error(ErrorUtils.printParseErrors(result)))
    }
  }

  def Input = rule { WhiteSpace ~ Expression ~ EOI }
  def Expression: Rule1[Double] = rule { Addition }

  def Addition = rule {
    Multiplication ~ zeroOrMore(
      "+" ~ WhiteSpace ~ Multiplication ~~> ((a: Double, b) ⇒ a + b) |
        "-" ~ WhiteSpace ~ Multiplication ~~> ((a: Double, b) ⇒ a - b)
    )
  }

  def Multiplication = rule {
    Factor ~ zeroOrMore(
      "*" ~ WhiteSpace ~ Factor ~~> ((a: Double, b) ⇒ a * b) |
        "/" ~ WhiteSpace ~ Factor ~~> ((a: Double, b) ⇒ a / b)
    )
  }

  def Factor = rule { Number | Parens }
  def Parens = rule { "(" ~ WhiteSpace ~ Expression ~ ")" ~ WhiteSpace }

  def Number = rule { group(optional("-" | "+") ~ UnsignedNumber) ~> (_.replace(',', '.').toDouble) ~ WhiteSpace }
  def UnsignedNumber = rule { Frac | (oneOrMore(Digit) ~ optional(Frac)) }
  def Frac = rule { anyOf(".,") ~ oneOrMore(Digit) }
  def Digit = rule { "0" - "9" }

  def WhiteSpace = rule { zeroOrMore((" " | "\t" | "\r" | "\n" | "\f" | "\b") label "WhiteSpace") }

}
