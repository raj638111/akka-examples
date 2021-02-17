name := "akka-examples"
scalaVersion := "2.12.8"

val akkaVersion = "2.6.12"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
	"com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) =>
   MergeStrategy.discard
 case x =>
   MergeStrategy.first
}

