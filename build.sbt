scalaVersion := "2.11.7"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources()

libraryDependencies += "org.pegdown" % "pegdown" % "1.4.2" % "test"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6" withSources()

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.3" withSources()

lazy val openTestsReport: TaskKey[Unit] = taskKey("Open HTML report for tests")

openTestsReport := {
  println("Opening test reports...")
  "open target/html/index.html".!
}

lazy val exo1: TaskKey[Unit] = taskKey("Launch EXO 1")

exo1 := {(testOnly in Test).toTask(" -- -n EXO_4_1 -h target/html").value}

(openTestsReport in exo1) <<= openTestsReport triggeredBy exo1

lazy val exo2: TaskKey[Unit] = taskKey("Launch EXO 2")

exo2 := {(testOnly in Test).toTask(" -- -n EXO_4_2 -h target/html").value}

(openTestsReport in exo2) <<= openTestsReport triggeredBy exo2

lazy val exo3: TaskKey[Unit] = taskKey("Launch EXO 3")

exo3 := {(testOnly in Test).toTask(" -- -n EXO_4_3 -h target/html").value}

(openTestsReport in exo3) <<= openTestsReport triggeredBy exo3

lazy val exo4: TaskKey[Unit] = taskKey("Launch EXO 4")

exo4 := {(testOnly in Test).toTask(" -- -n EXO_4_4 -h target/html").value}

(openTestsReport in exo4) <<= openTestsReport triggeredBy exo4

lazy val exo5: TaskKey[Unit] = taskKey("Launch EXO 5")

exo5 := {(testOnly in Test).toTask(" -- -n EXO_4_5 -h target/html").value}

(openTestsReport in exo5) <<= openTestsReport triggeredBy exo5

lazy val exo6: TaskKey[Unit] = taskKey("Launch EXO 6")

exo6 := {(testOnly in Test).toTask(" -- -n EXO_4_6 -h target/html").value}

(openTestsReport in exo6) <<= openTestsReport triggeredBy exo6

lazy val exo7: TaskKey[Unit] = taskKey("Launch EXO 7")

exo7 := {(testOnly in Test).toTask(" -- -n EXO_4_7 -h target/html").value}

(openTestsReport in exo7) <<= openTestsReport triggeredBy exo7

lazy val exo8: TaskKey[Unit] = taskKey("Launch EXO 8")

exo8 := {(testOnly in Test).toTask(" -- -n EXO_4_8 -h target/html").value}

(openTestsReport in exo8) <<= openTestsReport triggeredBy exo8

lazy val exo9: TaskKey[Unit] = taskKey("Launch EXO 9")

exo9 := {(testOnly in Test).toTask(" -- -n EXO_4_9 -h target/html").value}

(openTestsReport in exo9) <<= openTestsReport triggeredBy exo9