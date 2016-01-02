scalaVersion := "2.11.7"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" withSources()

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6" withSources()

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.3" withSources()

lazy val exo1: TaskKey[Unit] = taskKey("Launch EXO 1")

exo1 := {(testOnly in Test).toTask(" -- -n EXO_3_1").value}


lazy val exo2: TaskKey[Unit] = taskKey("Launch EXO 2")

exo2 := {(testOnly in Test).toTask(" -- -n EXO_3_2").value}


lazy val exo3: TaskKey[Unit] = taskKey("Launch EXO 3")

exo3 := {(testOnly in Test).toTask(" -- -n EXO_3_3").value}


lazy val exo4: TaskKey[Unit] = taskKey("Launch EXO 4")

exo4 := {(testOnly in Test).toTask(" -- -n EXO_3_4").value}


lazy val exo5: TaskKey[Unit] = taskKey("Launch EXO 5")

exo5 := {(testOnly in Test).toTask(" -- -n EXO_3_5").value}


lazy val exo6: TaskKey[Unit] = taskKey("Launch EXO 6")

exo6 := {(testOnly in Test).toTask(" -- -n EXO_3_6").value}

lazy val exo7: TaskKey[Unit] = taskKey("Launch EXO 7")

exo7 := {(testOnly in Test).toTask(" -- -n EXO_3_7").value}

lazy val exo8: TaskKey[Unit] = taskKey("Launch EXO 8")

exo8 := {(testOnly in Test).toTask(" -- -n EXO_3_8").value}

lazy val exo9: TaskKey[Unit] = taskKey("Launch EXO 9")

exo9 := {(testOnly in Test).toTask(" -- -n EXO_3_9").value}

lazy val exo10: TaskKey[Unit] = taskKey("Launch EXO 10")

exo10 := {(testOnly in Test).toTask(" -- -n EXO_3_10").value}