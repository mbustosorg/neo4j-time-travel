name := "Neo4JTimeTravel"

version := "1.0"

scalaVersion := "2.11.4"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val sprayV = "1.3.2"
  val akkaV = "2.3.6"
  Seq(
  "org.anormcypher" %% "anormcypher" % "0.6.0",
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-http"    % sprayV,
    "io.spray"            %%  "spray-httpx"   % sprayV,
    "io.spray"            %%  "spray-util"    % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.slick"  %%  "slick"         % "2.1.0",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "log4j"               %   "log4j"         % "1.2.14",
    "org.slf4j"           %   "slf4j-api"     % "1.7.6",
    "org.slf4j"           %   "slf4j-simple"  % "1.7.6",
    "joda-time"           % "joda-time"       % "2.7",
    "org.joda"            % "joda-convert"    % "1.2")
}