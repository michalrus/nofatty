lazy val root = (
  Common.Project("root")
  settings(
    resolvers += R.TypeSafe,
    resolvers += R.Spray,
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      D.ScalaTest % "test",
      D.ScalaCheck % "test",
      D.JFreeChart,
      D.JodaTime, D.JodaConvert,
      D.Parboiled,
      D.SQLite,
      D.Slick, D.Slf4j,
      "com.github.tototoshi" %% "scala-csv" % "1.2.0"
    )
  )
)
