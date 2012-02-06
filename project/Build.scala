import sbt._
import Keys._
import samskivert.ProjectBuilder

object PlayNBuild extends Build {
  val builder = new ProjectBuilder("pom.xml") {
    override val globalSettings = Seq(
      crossPaths   := false,
      scalaVersion := "2.9.1",
      javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      fork in Compile := true,
      autoScalaLibrary := false, // no scala-library dependency
      publishArtifact in (Compile, packageDoc) := false, // no scaladocs; it fails
      resolvers    += "Forplay Legacy" at "http://forplay.googlecode.com/svn/mavenrepo"
    )
    override def projectSettings (name :String) = name match {
      case "core" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedBase <<= baseDirectory { base => base / "disabled" },
        libraryDependencies ++= Seq(
          // scala test dependencies
 	        "com.novocode" % "junit-interface" % "0.7" % "test->default"
        )
      )
      case "gwtbox2d" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        // exclude GWT generator code
        excludeFilter in unmanagedSources ~= { _ || "PoolingStackGenerator.java" },
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "webgl" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "html" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
        // adds source files to our jar file (needed by GWT)
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src"
      )
      case "flash" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedSourceDirectories in Test <+= baseDirectory / "tests"
      )
      case "java" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedSourceDirectories in Test <+= baseDirectory / "tests"
      )
      case "android" => Seq(
        unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
        unmanagedSourceDirectories in Test <+= baseDirectory / "tests"
      )
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

  // one giant fruit roll-up to bring them all together
  lazy val playn = Project("playn", file(".")) aggregate(
    core, gwtbox2d, webgl, java, html, flash, android)
}
