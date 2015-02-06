import sbt._

object D {
  val ScalaTest = "org.scalatest" %% "scalatest" % V.ScalaTest
  val ScalaCheck = "org.scalacheck" %% "scalacheck" % V.ScalaCheck
  val JFreeChart = "org.jfree" % "jfreechart" % V.JFreeChart
  val JodaTime = "joda-time" % "joda-time" % V.JodaTime
  val JodaConvert = "org.joda" % "joda-convert" % V.JodaConvert
}

object R {
  val TypeSafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val Spray = "spray repo" at "http://repo.spray.io"
  val MichalRus = "michalrus.com repo" at "http://maven.michalrus.com/"
}
