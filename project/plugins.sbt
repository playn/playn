// pom-util POM helpers
libraryDependencies += "com.samskivert" % "sbt-pom-util" % "0.6-SNAPSHOT"

// TEMP: force use of locally installed version as mine has gwt-superdevmode
// a Maven repo needed for the sbt-gwt-plugin
// resolvers += "thunderklaus repo" at "http://thunderklaus.github.com/maven"

// this wires up JRebel; start tests with JRebel via: tests-java/re-start
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

// handles GWT stuff for test-html build (also pulls in xsbt-web-plugin)
// addSbtPlugin("net.thunderklaus" % "sbt-gwt-plugin" % "1.1-SNAPSHOT")
