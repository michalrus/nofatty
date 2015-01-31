import sbt._

object D {
  val ScalaTest = "org.scalatest" %% "scalatest" % V.ScalaTest
  val ScalaCheck = "org.scalacheck" %% "scalacheck" % V.ScalaCheck
  val AkkaActor = "com.typesafe.akka" %% "akka-actor" % V.Akka
  val AkkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
  val SprayCan = "io.spray" %% "spray-can" % V.Spray
  val SprayRouting = "io.spray" %% "spray-routing" % V.Spray
  val SprayClient = "io.spray" %% "spray-client" % V.Spray
  val ScalaXML = "org.scala-lang.modules" %% "scala-xml" % V.ScalaXML
}

object R {
  val TypeSafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val Spray = "spray repo" at "http://repo.spray.io"
  val MichalRus = "michalrus.com repo" at "http://maven.michalrus.com/"
}
