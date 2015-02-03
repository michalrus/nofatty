lazy val root = (
  Common.Project("root")
  settings(
    resolvers += R.TypeSafe,
    resolvers += R.Spray,
    libraryDependencies ++= Seq(
      D.ScalaTest % "test",
      D.ScalaCheck % "test",
      D.JFreeChart,
      D.JCalendar
    )
  )
)
