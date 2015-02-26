name := "Neo4JTimeTravel"

version := "1.0"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.4.4",
  "com.stackmob" %% "newman" % "1.3.5"
)