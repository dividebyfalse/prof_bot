import com.typesafe.sbt.SbtStartScript

name := "prof_bot"

version := "1.0"

scalaVersion := "2.11.8"
libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.3.0"
  )
seq(SbtStartScript.startScriptForClassesSettings: _*)
