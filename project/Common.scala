import sbt._
import Keys._
import wartremover.WartRemover.autoImport._

object Common {

  val settings = Seq(
    organization := "com.michalrus",
    name := "nofatty",

    scalaVersion := "2.11.5",
    scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint",
      "-Xfatal-warnings",
      "-Yno-adapted-args", "-Yrangepos", "-Ywarn-dead-code", "-Ywarn-inaccessible",
      "-Ywarn-infer-any", "-Ywarn-nullary-override", "-Ywarn-numeric-widen",
      "-Ywarn-unused", "-Ywarn-unused-import", "-Ywarn-value-discard"),

    resolvers ++= Seq(R.MichalRus),

    wartremoverErrors ++= Warts.allBut(Wart.Nothing, Wart.Any, Wart.NoNeedForMonad),

    autoAPIMappings := true
  )

  def Project(id: String) =
    sbt.Project(id, sbt.file(if (id == "root") "." else id)).
    settings(Common.settings: _*)

}
