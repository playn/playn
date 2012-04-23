import sbt._
import Keys._
import samskivert.ProjectBuilder

object PlayNBuild extends Build {
  val builder = new ProjectBuilder("pom.xml") {
    override val globalSettings = Seq(
      crossPaths   := false,
      scalaVersion := "2.9.1",
      javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      javaOptions ++= Seq("-ea"),
      fork in Compile := true,
      autoScalaLibrary := false, // no scala-library dependency
      publishArtifact in (Compile, packageDoc) := false, // no scaladocs; it fails
      resolvers    += "Forplay Legacy" at "http://forplay.googlecode.com/svn/mavenrepo",
      // everybody gets their source and tests directories rewritten
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests"
    )
    override def projectSettings (name :String) = name match {
      case "core" => Seq(
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedBase <<= baseDirectory { base => base / "disabled" },
        libraryDependencies ++= Seq(
          // scala test dependencies
 	        "com.novocode" % "junit-interface" % "0.7" % "test->default"
        )
      )
      case "gwtbox2d" => Seq(
        // exclude GWT generator code
        excludeFilter in unmanagedSources ~= { _ || "PoolingStackGenerator.java" },
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "webgl" => Seq(
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "html" => Seq(
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "tests-core" => Seq(
        // copy resources from playn/tests/resources
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
        excludeFilter in unmanagedResources ~= { _ || "*.java" }
      )
      case "tests-java" => LWJGLPlugin.lwjglSettings
      case _ => Nil
    }
  }

  lazy val core = builder("core")
  lazy val gwtbox2d = builder("gwtbox2d")
  lazy val webgl = builder("webgl")
  lazy val java = builder("java")
  lazy val html = builder("html")
  lazy val flash = builder("flash")
  lazy val android = builder("android")
  lazy val ios = builder("ios")
  lazy val testsCore = builder("tests-core")
  lazy val testsJava = builder("tests-java")

  // one giant fruit roll-up to bring them all together
  lazy val playn = Project("playn", file(".")) aggregate(
    core, gwtbox2d, webgl, java, html, flash, android, ios, testsCore, testsJava)
}
