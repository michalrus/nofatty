import sbt._

object D {
  val ScalaTest = "org.scalatest" %% "scalatest" % V.ScalaTest
  val ScalaCheck = "org.scalacheck" %% "scalacheck" % V.ScalaCheck
  val JFreeChart = "org.jfree" % "jfreechart" % V.JFreeChart
  val JodaTime = "joda-time" % "joda-time" % V.JodaTime
  val JodaConvert = "org.joda" % "joda-convert" % V.JodaConvert
  val Parboiled = "org.parboiled" %% "parboiled-scala" % V.Parboiled
  val SQLite = "org.xerial" % "sqlite-jdbc" % V.SQLite
  val Slick = "com.typesafe.slick" %% "slick" % V.Slick
  val Slf4j = "org.slf4j" % "slf4j-nop" % V.Slf4j
}

object R {
  val TypeSafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val Spray = "spray repo" at "http://repo.spray.io"
  val MichalRus = "michalrus.com repo" at "http://maven.michalrus.com/"
}
