//  addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

organization := "net.bhardy"

name := "Nitpick"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.0"

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
    "org.scalatra" %% "scalatra" % "2.2.0",
    "org.scalatra" %% "scalatra-scalate" % "2.2.0",
    "org.scalatra" %% "scalatra-specs2" % "2.2.0" % "test",
    "org.scalatra" %% "scalatra-json" % "2.2.0",
    "org.json4s" %% "json4s-jackson" % "3.1.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "2.2.0.201212191850-r",
    "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container;test",
    "org.scalatest" %% "scalatest" % "1.9.1"  % "test",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
).map{
    _.exclude("com.typesafe.akka", "akka-actor")
}

