name := "akka-examples"
scalaVersion := "2.13.8"

val akkaVersion = "2.6.12"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
	"com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.apache.commons" % "commons-lang3" % "3.11",
  "org.apache.logging.log4j" % "log4j-core" % "2.17.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

assemblyMergeStrategy in assembly := {
  case n if n.startsWith("reference.conf") =>
    MergeStrategy.concat
  case PathList(x @ "META-INF", y @ "services",xs @ _*) =>
		MergeStrategy.filterDistinctLines
  case PathList("META-INF",xs @ _*) => MergeStrategy.discard  
  case _ => MergeStrategy.first
}


